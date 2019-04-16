/****************************************************************************************
 * Copyright (c) 2016, 2017, 2019 Vincent Hiribarren                                    *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * Linking Beacon Simulator statically or dynamically with other modules is making      *
 * a combined work based on Beacon Simulator. Thus, the terms and conditions of         *
 * the GNU General Public License cover the whole combination.                          *
 *                                                                                      *
 * As a special exception, the copyright holders of Beacon Simulator give you           *
 * permission to combine Beacon Simulator program with free software programs           *
 * or libraries that are released under the GNU LGPL and with independent               *
 * modules that communicate with Beacon Simulator solely through the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces. You may           *
 * copy and distribute such a system following the terms of the GNU GPL for             *
 * Beacon Simulator and the licenses of the other code concerned, provided that         *
 * you include the source code of that other code when and as the GNU GPL               *
 * requires distribution of source code and provided that you do not modify the         *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces.                   *
 *                                                                                      *
 * The intent of this license exception and interface is to allow Bluetooth low energy  *
 * closed or proprietary advertise data packet structures and contents to be sensibly   *
 * kept closed, while ensuring the GPL is applied. This is done by using an interface   *
 * which only purpose is to generate android.bluetooth.le.AdvertiseData objects.        *
 *                                                                                      *
 * This exception is an additional permission under section 7 of the GNU General        *
 * Public License, version 3 (“GPLv3”).                                                 *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package net.alea.beaconsimulator.bluetooth;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import net.alea.beaconsimulator.ActivityMain;
import net.alea.beaconsimulator.App;
import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.event.BeaconChangedEvent;
import net.alea.beaconsimulator.bluetooth.event.BeaconDeletedEvent;
import net.alea.beaconsimulator.bluetooth.event.BroadcastChangedEvent;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.BeaconType;
import net.alea.beaconsimulator.event.UserRequestStartEvent;
import net.alea.beaconsimulator.event.UserRequestStopAllEvent;
import net.alea.beaconsimulator.event.UserRequestStopEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;


public class BeaconSimulatorService extends Service {

    private static final Logger sLogger = LoggerFactory.getLogger(BeaconSimulatorService.class);

    private static final String PREFIX = "net.alea.beaconsimulator.service.";
    public static final String ACTION_START = PREFIX + "ACTION_START";
    public static final String ACTION_SCHEDULED = PREFIX + "ACTION_SCHEDULED";
    public static final String ACTION_STOP = PREFIX + "ACTION_STOP";
    public static final String ACTION_STOP_ALL = PREFIX + "ACTION_STOP_ALL";
    public static final String EXTRA_BEACON_STORE_ID = PREFIX + "EXTRA_BEACON_STORE_ID";
    public static final String EXTRA_USER_TRIGGERED = PREFIX + "EXTRA_USER_TRIGGERED";

    public static final int NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "status";

    private static BeaconSimulatorService sInstance;

    private AlarmManager mAlarmManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeAdvertiser mBtAdvertiser;
    private Map<UUID, AdvertiseCallback> mAdvertiseCallbacks;
    private Map<UUID, Long> mAdvertiseStartTimestamp;
    private Map<UUID, PendingIntent> mScheduledPendingIntents;
    private BeaconStore mBeaconStore;

    // TODO Should I deprecate ServiceControl in favor of static methods?
    public class ServiceControl extends Binder {
        /* FIXME Warning, 3 ways to know number of running beacons: ServiceControl, BeaconStore running list, BroadcastChangeEvent
         * ServiceControl is for current status, BeaconStore to persist status, BroadcastChangEvent for changes in status. */
        public Set<UUID> getBroadcastList() {
            return Collections.unmodifiableSet(mAdvertiseCallbacks.keySet());
        }
        public boolean isBroadcasting() {
            return mAdvertiseCallbacks.size() != 0;
        }
    }

    private final ServiceControl mBinder = new ServiceControl();

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    final int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (btState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            final boolean resilientMode = App.getInstance().getConfig().getBroadcastResilience();
                            BeaconSimulatorService.stopAll(context, !resilientMode);
                            break;
                    }
                    break;
                }
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        mBtAdapter = ((BluetoothManager)getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        mAdvertiseCallbacks = new TreeMap<>();
        mAdvertiseStartTimestamp = new HashMap<>();
        mScheduledPendingIntents = new HashMap<>();
        mBeaconStore = ((App)getApplication()).getBeaconStore();
        registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        EventBus.getDefault().register(this);
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateNotification(); // Called as soon as possible to avoid ANR: startForegroundService/startForeground sequence
        String action = intent.getAction();
        switch (action) {
            case ACTION_START: {
                sLogger.info("Action: starting new broadcast");
                final UUID id = ((ParcelUuid)intent.getParcelableExtra(EXTRA_BEACON_STORE_ID)).getUuid();
                startBroadcast(startId, id, false);
                break;
            }
            case ACTION_SCHEDULED: {
                sLogger.info("Action: processing scheduled update");
                final UUID id = ((ParcelUuid)intent.getParcelableExtra(EXTRA_BEACON_STORE_ID)).getUuid();
                final BeaconModel model = mBeaconStore.getBeacon(id);
                if (mAdvertiseCallbacks.containsKey(model.getId())) {
                    updateBroadcast(startId, model.getId());
                }
                else {
                    sLogger.info("No more broadcasting this, skipping");
                }
                break;
            }
            case ACTION_STOP: {
                sLogger.debug("Action: stopping a broadcast");
                final UUID id = ((ParcelUuid)intent.getParcelableExtra(EXTRA_BEACON_STORE_ID)).getUuid();
                stopBroadcast(startId, id, false, false);
                break;
            }
            case ACTION_STOP_ALL: {
                sLogger.debug("Action: stopping all broadcasts");
                final boolean userTriggered = intent.getBooleanExtra(EXTRA_USER_TRIGGERED, false);
                if (userTriggered) {
                    EventBus.getDefault().post(new UserRequestStopAllEvent());
                }
                stopAll(startId, false);
                break;
            }
            default: {
                sLogger.warn("Unknown action asked");
            }

        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        sInstance = null;
        super.onDestroy();
        sLogger.debug("onDestroy() called");
        stopAll(0, true);
        unregisterReceiver(mBroadcastReceiver);
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onBeaconDeleted(BeaconDeletedEvent event) {
        if (mAdvertiseCallbacks.containsKey(event.getBeaconId())) {
            BeaconSimulatorService.stopBroadcast(this, event.getBeaconId(), true);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onBeaconChanged(BeaconChangedEvent event) {
        if (mAdvertiseCallbacks.containsKey(event.getBeaconId())) {
            BeaconSimulatorService.stopBroadcast(this, event.getBeaconId(), false);
            BeaconSimulatorService.startBroadcast(this, event.getBeaconId(), false);
        }
    }

    private void updateBroadcast(int serviceStartId, UUID id) {
        stopBroadcast(serviceStartId, id, true, false);
        startBroadcast(serviceStartId, id, true);
    }

    private void startBroadcast(int serviceStartId, UUID id, boolean isRestart) {
        if ( !isRestart && mAdvertiseCallbacks.containsKey(id)) {
            sLogger.info("Already broadcasting this beacon model, skipping");
            return;
        }
        final BeaconModel model = mBeaconStore.getBeacon(id);
        if (model == null) {
            sLogger.info("This beacon does not exist any more");
            return;
        }
        if (BeaconType.eddystoneEID.equals(model.getType())) { // TODO I do not like it is here
            setScheduledUpdate(id, model.getEddystoneEID().getNextChangeTimestampMilliseconds());
        }
        mBtAdvertiser = mBtAdapter.getBluetoothLeAdvertiser();
        if (mBtAdvertiser == null || !mBtAdapter.isEnabled()) {
            sLogger.warn("Bluetooth is off, doing nothing");
            EventBus.getDefault().post(new BroadcastChangedEvent(id, false, mAdvertiseCallbacks.size()));
            return;
        }
        final AdvertiseSettings settings = model.getSettings().generateADSettings();
        final ExtendedAdvertiseData exAdvertiseData =  model.generateADData();
        final AdvertiseData advertiseData = exAdvertiseData.getAdvertiseData();
        final String localName = exAdvertiseData.getLocalName();
        if (exAdvertiseData.getAdvertiseData().getIncludeDeviceName()) {
            final String backupName = mBtAdapter.getName();
            if (localName != null) {
                mBtAdapter.setName(localName);
            }
            mBtAdvertiser.startAdvertising(settings, advertiseData, new MyAdvertiseCallback(serviceStartId, id, isRestart));
            mBtAdapter.setName(backupName);
        }
        else {
            mBtAdvertiser.startAdvertising(settings, advertiseData, new MyAdvertiseCallback(serviceStartId, id, isRestart));
        }
    }



    private void stopBroadcast(int serviceStartId, UUID id, boolean isRestart, boolean ignoreServiceStartId) {
        final AdvertiseCallback adCallback = mAdvertiseCallbacks.get(id);
        if (adCallback != null) {
            try {
                if (mBtAdvertiser != null) {
                    mBtAdvertiser.stopAdvertising(adCallback);
                }
                else {
                    sLogger.warn("Not able to stop broadcast; mBtAdvertiser is null");
                }
            }
            catch(RuntimeException e) { // Can happen if BT adapter is not in ON state
                sLogger.warn("Not able to stop broadcast; BT state: {}", mBtAdapter.isEnabled(), e);
            }
            removeScheduledUpdate(id);
            mAdvertiseCallbacks.remove(id);
            if (! isRestart) {
                EventBus.getDefault().post(new BroadcastChangedEvent(id, false, mAdvertiseCallbacks.size()));
                // For Fabric Answers
                long totalTime = (SystemClock.elapsedRealtime() - mAdvertiseStartTimestamp.get(id)) / 1000;
                mAdvertiseStartTimestamp.remove(id);
                BeaconModel beaconModel = mBeaconStore.getBeacon(id);
                if (beaconModel != null) {
                    sLogger.info(String.format("Total broadcast time for beacon %s: %,ds", beaconModel.getId(), totalTime));
                }
            }
        }
        if (isRestart) {
            return;
        }
        if (mAdvertiseCallbacks.size() == 0) {
            stopForeground(true);
            if (! ignoreServiceStartId) {
                stopSelf(serviceStartId);
            }
            else {
                stopSelf();
            }
            sLogger.info("No more broadcast, stopping simulator service");
        }
        else {
            updateNotification();
        }
    }

    private void stopAll(int serviceId, boolean ignoreServiceId) {
        sLogger.info("Stopping all broadcasts");
        Set<UUID> idSet =  new TreeSet<>(mAdvertiseCallbacks.keySet()) ;
        for(UUID id: idSet){
            stopBroadcast(serviceId, id, false, ignoreServiceId);
        }
        mAdvertiseCallbacks.clear();
        mAdvertiseStartTimestamp.clear();
    }

    private void setScheduledUpdate(UUID id, long timestamp) {
        removeScheduledUpdate(id);
        Intent intent = new Intent(getApplicationContext(), BeaconSimulatorService.class);
        intent.putExtra(EXTRA_BEACON_STORE_ID, new ParcelUuid(id));
        intent.setAction(ACTION_SCHEDULED);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mScheduledPendingIntents.put(id, pendingIntent);
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
    }

    private void removeScheduledUpdate(UUID uuid) {
        PendingIntent pendingIntent = mScheduledPendingIntents.remove(uuid);
        if (pendingIntent == null) {
            return;
        }
        mAlarmManager.cancel(pendingIntent);
    }


    public static void startBroadcast(Context context, UUID uuid, boolean updateActiveList) {
        if (updateActiveList) {
            EventBus.getDefault().post(new UserRequestStartEvent(uuid));
        }
        final Intent intent = new Intent(context, BeaconSimulatorService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_BEACON_STORE_ID, new ParcelUuid(uuid));
        ContextCompat.startForegroundService(context, intent);
    }

    public static void stopBroadcast(Context context, UUID uuid, boolean updateActiveList) {
        if (updateActiveList) {
            EventBus.getDefault().post(new UserRequestStopEvent(uuid));
        }
        final Intent intent = new Intent(context, BeaconSimulatorService.class);
        intent.setAction(ACTION_STOP);
        intent.putExtra(EXTRA_BEACON_STORE_ID, new ParcelUuid(uuid));
        ContextCompat.startForegroundService(context, intent);
    }

    public static void stopAll(Context context, boolean updateActiveList) {
        final Intent intent = new Intent(context, BeaconSimulatorService.class);
        intent.setAction(ACTION_STOP_ALL);
        intent.putExtra(EXTRA_USER_TRIGGERED, updateActiveList);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, BeaconSimulatorService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbindService(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    public static boolean isBluetoothOn(Context context) {
        final BluetoothAdapter bluetoothAdapter = ((BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }

    public static boolean isBroadcastAvailable(Context context) {
        final BluetoothAdapter bluetoothAdapter = ((BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    public static Set<UUID> getBroadcastList() {
        if (sInstance != null) {
            return new TreeSet<>(sInstance.mAdvertiseCallbacks.keySet());
        }
        else {
            return Collections.emptySet();
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isBroadcasting() {
        if (sInstance != null) {
            return sInstance.mAdvertiseCallbacks.size() != 0;
        }
        else {
            return false;
        }
    }


    private class MyAdvertiseCallback extends AdvertiseCallback {
        private final UUID _id;
        private final int _serviceStartId;
        private final boolean _isRestart;
        public MyAdvertiseCallback(int serviceStartId, UUID id, boolean isRestart) {
            _id = id;
            _serviceStartId = serviceStartId;
            _isRestart = isRestart;
        }
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            mAdvertiseCallbacks.put(_id, this);
            if (! _isRestart) {
                mAdvertiseStartTimestamp.put(_id, SystemClock.elapsedRealtime());
                EventBus.getDefault().post(new BroadcastChangedEvent(_id, true, mAdvertiseCallbacks.size()));
                // Prepare and display notification
                updateNotification();
            }
            sLogger.info("Success in starting broadcast, currently active: {}", mAdvertiseCallbacks.size());
        }
        public void onStartFailure(int errorCode) {
            EventBus.getDefault().post(new BroadcastChangedEvent(_id, false, mAdvertiseCallbacks.size(), true));
            int reason;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    reason = R.string.advertise_error_already_started;
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    reason = R.string.advertise_error_data_large;
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    reason = R.string.advertise_error_unsupported;
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    reason = R.string.advertise_error_internal;
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    reason = R.string.advertise_error_too_many;
                    break;
                default:
                    reason = R.string.advertise_error_unknown;
            }
            Toast.makeText(BeaconSimulatorService.this, reason, Toast.LENGTH_SHORT).show();
            sLogger.warn("Error starting broadcasting: {}", reason);
            mAdvertiseStartTimestamp.remove(_id);
            if (mAdvertiseCallbacks.size() == 0) {
                stopForeground(true);
                stopSelf(_serviceStartId);
            }
        }
    }

    private void updateNotification() {
        final Intent activityIntent = new Intent(BeaconSimulatorService.this, ActivityMain.class);
        final PendingIntent activityPendingIntent = PendingIntent.getActivity(BeaconSimulatorService.this, 0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        final Intent stopBroadcastIntent = new Intent(BeaconSimulatorService.this, BeaconSimulatorService.class);
        stopBroadcastIntent.setAction(ACTION_STOP_ALL);
        stopBroadcastIntent.putExtra(EXTRA_USER_TRIGGERED, true);
        final PendingIntent stopBroadcastPendingIntent = PendingIntent.getService(BeaconSimulatorService.this, 0, stopBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel creation
            final CharSequence name = getString(R.string.notif_channel_name);
            final String description = getString(R.string.notif_channel_description);
            final int importance = NotificationManager.IMPORTANCE_LOW;
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            final NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        final NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(BeaconSimulatorService.this, CHANNEL_ID);
        notifBuilder
                .setSmallIcon(R.drawable.ic_app_plain)
                //.setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getResources().getQuantityString(R.plurals.notif_message, mAdvertiseCallbacks.size(), mAdvertiseCallbacks.size()))
                .addAction(R.drawable.ic_menu_pause, getString(R.string.notif_action_stop), stopBroadcastPendingIntent)
                .setContentIntent(activityPendingIntent);
        startForeground(NOTIFICATION_ID, notifBuilder.build());
    }


}

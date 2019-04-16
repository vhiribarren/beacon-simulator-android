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


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Handler;

import net.alea.beaconsimulator.App;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BeaconDiagnostic {


    private static final Logger sLogger = LoggerFactory.getLogger(BeaconDiagnostic.class);

    public static final int REASON_MAX_TRIES = 1;
    public static final int REASON_IS_RUNNING = 2;
    public static final int REASON_BLE_UNSUPPORTED = 3;
    public static final int REASON_UNEXPECTED_ERROR = 4;
    public static final int REASON_BT_OFF = 5;

    public interface OnDiagnosticResult {
        void onDiagnosticSuccess(int count);
        void onDiagnosticFailure(int reason);
    }


    private static final int MAX_TRIES = 128;

    private int mAdvertCount = 0;
    private Boolean mIsRunning = false;
    private List<AdvertiseCallback> mBleCallbacks;
    private BluetoothAdapter mBtAdapter;
    private BluetoothLeAdvertiser mBtAdvertiser;
    private OnDiagnosticResult mDiagnosticResultCallback;
    private Set<String> mRunningBeaconsState;


    private final Handler mHandler = new Handler();


    private final Runnable mIncreaseBeacons = new Runnable() {
        final AdvertiseSettings _adSettings = new AdvertiseSettings.Builder().build();
        final AdvertiseData _adData = new AdvertiseData.Builder().build();
        @Override
        public void run() {
            sLogger.debug("Scheduling new broadcast avertising");
            if (mBtAdapter.isEnabled() &&  mBtAdvertiser != null) {
                mBtAdvertiser.startAdvertising(_adSettings, _adData, new MyAdvertiseCallback());
            }
            else {
                mDiagnosticResultCallback.onDiagnosticFailure(REASON_BT_OFF);
                cleanAll();
            }
        }
    };


    private class MyAdvertiseCallback extends AdvertiseCallback {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            mBleCallbacks.add(this);
            mAdvertCount++;
            sLogger.debug("Success, current count is: {}", getLastAdvertCount());
            if (mBleCallbacks.size() < MAX_TRIES) {
                mHandler.post(mIncreaseBeacons);
            }
            else {
                sLogger.debug("Max try reached, stopping");
                mDiagnosticResultCallback.onDiagnosticFailure(REASON_MAX_TRIES);
                cleanAll();
            }
        }
        @Override
        public void onStartFailure(int errorCode) {
            int reason;
            switch (errorCode) {
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    sLogger.debug("Too many advertiser, stopping");
                    mDiagnosticResultCallback.onDiagnosticSuccess(mBleCallbacks.size());
                    cleanAll();
                    return;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    reason = REASON_BLE_UNSUPPORTED;
                    break;
                case ADVERTISE_FAILED_ALREADY_STARTED:
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                default:
                    reason = REASON_UNEXPECTED_ERROR;
            }
            sLogger.debug("Failure, reason code: {}", errorCode);
            mDiagnosticResultCallback.onDiagnosticFailure(reason);
            cleanAll();
        }
    }


    private void cleanAll() {
        sLogger.debug("Stopping all broadcasts");
        for (AdvertiseCallback adCallback : mBleCallbacks) {
            try {
                mBtAdvertiser.stopAdvertising(adCallback);
            }
            catch (Exception e) {
                sLogger.warn("Error while stopping advertisement", e);
            }
        }
        mBleCallbacks.clear();
        mIsRunning = false;
        restoreBeaconState();
    }

    private void saveBeaconState() {
        mRunningBeaconsState = App.getInstance().getBeaconStore().activeBeacons();
    }

    private void restoreBeaconState() {
        for(String beaconId : mRunningBeaconsState) {
            BeaconSimulatorService.startBroadcast(App.getInstance().getApplicationContext(), UUID.fromString(beaconId), false);
        }
    }

    public synchronized void launchDiagnostic(Context context, final OnDiagnosticResult onResult) {
        sLogger.debug("launchDiagnostic()");

        mDiagnosticResultCallback = onResult;
        final Context appContext = context.getApplicationContext();

        if (mIsRunning) {
            sLogger.debug("launchDiagnostic() - already running, leaving");
            onResult.onDiagnosticFailure(REASON_IS_RUNNING);
            return;
        }

        if (! BeaconSimulatorService.isBluetoothOn(appContext)) {
            sLogger.debug("Bluetooth is off, leaving");
            onResult.onDiagnosticFailure(REASON_BT_OFF);
            return;
        }
        if (! BeaconSimulatorService.isBroadcastAvailable(appContext)) {
            sLogger.debug("BLE is unsupported, leaving");
            onResult.onDiagnosticFailure(REASON_BLE_UNSUPPORTED);
            return;
        }

        mAdvertCount = 0;
        mIsRunning = true;

        // Get broadcast state
        saveBeaconState();

        // Stop all ongoing broadcasts
        BeaconSimulatorService.stopAll(appContext, false);

        // Prepare broadcaster
        mBleCallbacks = new ArrayList<>();
        mBtAdapter = ((BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        mBtAdvertiser = mBtAdapter.getBluetoothLeAdvertiser();
        mHandler.post(mIncreaseBeacons);
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public int getLastAdvertCount() {
        return mAdvertCount;
    }

}

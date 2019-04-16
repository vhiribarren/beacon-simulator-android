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

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.alea.beaconsimulator.bluetooth.event.BeaconChangedEvent;
import net.alea.beaconsimulator.bluetooth.event.BeaconDeletedEvent;
import net.alea.beaconsimulator.bluetooth.event.BeaconStoreSizeEvent;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;


public class BeaconStore {

    private static final Logger sLogger = LoggerFactory.getLogger(BeaconStore.class);

    private final static int CURRENT_VERSION = 1;

    private final static String PREFS_FILENAME = "beacons";
    private final static String KEY_INDEX = "_index";
    private final static String KEY_VERSION = "_version";
    private final static String KEY_ACTIVE = "_active";

    private final SharedPreferences mPrefs;
    private final Gson mGson;

    private List<String> mIndex;
    private final Map<String, BeaconModel> mBeacons;


    public BeaconStore(Context context) {
        mGson = new Gson();
        mBeacons = new HashMap<>();
        mPrefs = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        if (mPrefs.contains(KEY_VERSION)) {
            int version = mPrefs.getInt(KEY_VERSION, 0);
            switch (version) {
                case CURRENT_VERSION:
                    break;
                default:
                    sLogger.warn("Unexpected version in beacon persistent file");
            }
        }
        else {
            // Init content
            sLogger.info("Initializing beacon persistent file");
            mPrefs.edit()
                  .putInt(KEY_VERSION, CURRENT_VERSION)
                  .putString(KEY_INDEX, "[]")
                  .apply();
        }
        loadAll();
        sLogger.trace("Loaded {} beacons from persistent file", mIndex.size());
    }

    private void loadAll() {
        String jsonIndex = mPrefs.getString(KEY_INDEX, "[]");
        sLogger.trace("Index of saved beacons: {}", jsonIndex);
        Type collectionType = new TypeToken<ArrayList<String>>(){}.getType();
        mIndex = mGson.fromJson(jsonIndex, collectionType);
        for (String id : mIndex) {
            String jsonBeacon = mPrefs.getString(id, null);
            sLogger.trace("Saved beacon: {}", jsonBeacon);
            BeaconModel beacon = BeaconModel.parseFromJson(jsonBeacon);
            mBeacons.put(id, beacon);
        }
        EventBus.getDefault().postSticky(new BeaconStoreSizeEvent(mIndex.size()));
    }

    private void saveIndex() {
        mPrefs.edit()
              .putString(KEY_INDEX, mGson.toJson(mIndex))
              .apply();
    }

    public BeaconModel getBeaconAt(int index) {
        if (index >= 0 && index < mIndex.size()) {
            String id = mIndex.get(index);
            return mBeacons.get(id);
        }
        else {
            return null;
        }
    }

    public BeaconModel getBeacon(UUID id) {
        return mBeacons.get(id.toString());
    }

    public int getBeaconIndex(UUID id) {
        return mIndex.indexOf(id.toString());
    }

    @SuppressWarnings("UnusedReturnValue")
    public int changeBeaconIndex(BeaconModel beacon, int newIndex) {
        int index = mIndex.indexOf(beacon.getId().toString());
        if (index < 0) {
            return index;
        }
        if (index >= mIndex.size()) {
            index = mIndex.size() - 1;
        }
        String id = mIndex.remove(index);
        mIndex.add(newIndex, id);
        saveIndex();
        return newIndex;
    }

    public int size() {
        return mIndex.size();
    }


    public void saveBeacon(BeaconModel beacon) {
        String id = beacon.getId().toString();
        if (!mIndex.contains(id)) {
            mIndex.add(id);
            saveIndex();
            EventBus.getDefault().postSticky(new BeaconStoreSizeEvent(mIndex.size()));
        }
        else {
            EventBus.getDefault()
                    .post(new BeaconChangedEvent(beacon.getId(), beacon.generateADData(), beacon.generateADSettings()));
        }
        mBeacons.put(id, beacon);
        mPrefs.edit()
                .putString(id, beacon.serializeToJson())
                .apply();
    }

    public void deleteBeacon(BeaconModel beacon) {
        String id = beacon.getId().toString();
        mBeacons.remove(id);
        mIndex.remove(id);
        mPrefs.edit()
              .remove(id)
              .apply();
        saveIndex();
        EventBus.getDefault().post(new BeaconDeletedEvent(beacon.getId()));
        EventBus.getDefault().postSticky(new BeaconStoreSizeEvent(mIndex.size()));
    }


    public void putActiveBeacon(UUID beacon) {
        final Set<String> currentSet = new TreeSet<>(mPrefs.getStringSet(KEY_ACTIVE, new TreeSet<String>()));
        currentSet.add(beacon.toString());
        mPrefs.edit()
                .putStringSet(KEY_ACTIVE, currentSet)
                .apply();
        sLogger.debug("Adding a beacon to active set, current state: {}", currentSet.toString());
    }

    public void removeActiveBeacon(UUID beacon) {
        final Set<String> currentSet = new TreeSet<>(mPrefs.getStringSet(KEY_ACTIVE, new TreeSet<String>()));
        currentSet.remove(beacon.toString());
        mPrefs.edit()
                .putStringSet(KEY_ACTIVE, currentSet)
                .apply();
        sLogger.debug("Removing a beacon from active set, current state: {}", currentSet.toString());
    }

    public void removeAllActiveBeacons() {
        mPrefs.edit()
                .putStringSet(KEY_ACTIVE, null)
                .apply();
        sLogger.debug("Removing all beacon from active state.");
    }

    public Set<String> activeBeacons() {
        return Collections.unmodifiableSet(mPrefs.getStringSet(KEY_ACTIVE, new TreeSet<String>()));
    }


}

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

package net.alea.beaconsimulator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.akexorcist.localizationactivity.LocalizationActivity;

import net.alea.beaconsimulator.bluetooth.BeaconSimulatorService;
import net.alea.beaconsimulator.bluetooth.BeaconStore;

import java.util.Locale;
import java.util.UUID;

public class FragmentSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Activity activity = getActivity();
        switch (key) {
            case Config.PREF_KEY_LANGUAGE: {
                if (activity != null) {
                    ((LocalizationActivity)activity).setLanguage(
                            sharedPreferences.getString(Config.PREF_KEY_LANGUAGE, Locale.getDefault().getLanguage())
                    );
                }
                break;
            }
            case Config.PREF_KEY_BROADCAST_RESILIENCE: {
                if ( activity != null && ! sharedPreferences.getBoolean(key, false) ) {
                    App.getInstance().enableRebootResilience(false);
                    // Take into account case where bluetooth is OFF with some resilient beacons (or ON with broadcasting beacons)
                    synchronizeActiveBeaconList();
                }
                else if ( activity != null && sharedPreferences.getBoolean(key, false) ) {
                    App.getInstance().enableRebootResilience(true);
                }
                break;
            }
            default:
        }
    }

    public void synchronizeActiveBeaconList() {
        final BeaconStore store = App.getInstance().getBeaconStore();
        store.removeAllActiveBeacons();
        for(UUID runningBeacon : BeaconSimulatorService.getBroadcastList()) {
            store.putActiveBeacon(runningBeacon);
        }
    }

}
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


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

public class Config {

    public final static String PREF_KEY_LANGUAGE = "pref_language";
    public final static String PREF_KEY_SCAN_KEEP_SCREEN_ON = "pref_scan_keep_screen_on";
    public final static String PREF_KEY_BROADCAST_RESILIENCE = "pref_broadcast_resilience";

    private final SharedPreferences mPrefs;

    public Config(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public Locale getLocale() {
        return new Locale(mPrefs.getString(PREF_KEY_LANGUAGE, Locale.getDefault().getLanguage()));
    }

    public boolean getBroadcastResilience() {
        return mPrefs.getBoolean(PREF_KEY_BROADCAST_RESILIENCE, false);
    }

    public void setBroadcastResilience(boolean resilient) {
        mPrefs
            .edit()
            .putBoolean(PREF_KEY_BROADCAST_RESILIENCE, resilient)
            .apply();
    }

    public boolean getKeepScreenOnForScan() {
        return mPrefs.getBoolean(PREF_KEY_SCAN_KEEP_SCREEN_ON, false);
    }

    public void setKeepScreenOnForScan(boolean value) {
        mPrefs
            .edit()
            .putBoolean(PREF_KEY_SCAN_KEEP_SCREEN_ON, value)
            .apply();
    }

}

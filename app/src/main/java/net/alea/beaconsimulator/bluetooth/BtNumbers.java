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
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class BtNumbers {


    private final static String JSON_COMPANY_IDS = "company_id.json";
    private final static String JSON_GAP_TYPES = "gap_type.json";
    private final static String JSON_SERVICE_16BIT = "service_uuid_16bit.json";

    private static final String BASE_UUID_FORMAT = "%02x%02x%02x%02x-0000-1000-8000-00805f9b34fb";

    private final Context mContext;
    private Map<String, String> mCompanyIds;
    private Map<String, String> mGapTypes;
    private Map<String, String> mServices;


    public BtNumbers(Context context) {
        mContext = context.getApplicationContext();
    }

    @Nullable
    public String convertCompanyId(int id) {
        if (mCompanyIds == null) {
           mCompanyIds = loadKeyValueJson(JSON_COMPANY_IDS);
        }
        return mCompanyIds.get(Integer.toString(id));
    }

    @Nullable
    public String convertGapType(int id) {
        if (mGapTypes == null) {
            mGapTypes = loadKeyValueJson(JSON_GAP_TYPES);
        }
        return mGapTypes.get(Integer.toString(id));
    }

    @Nullable
    public String convertServiceUuid(UUID uuid) {
        String stringId = uuid.toString().substring(0, 8);
        return convertServiceUuid(Long.parseLong(stringId, 16));
    }

    @Nullable
    public String convertServiceUuid(long value_16bit) {
        if (mServices == null) {
            mServices = loadKeyValueJson(JSON_SERVICE_16BIT);
        }
        return mServices.get(Long.toString(value_16bit));
    }

    private Map<String,String> loadKeyValueJson(String filename) {
        try(InputStream inputStream = mContext.getAssets().open(filename)) {
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(inputStream, "UTF-8");
            final char[] buffer = new char[1024];
            while(true) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            return gson.fromJson(out.toString(), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




}

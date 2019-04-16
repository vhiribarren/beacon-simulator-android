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

package net.alea.beaconsimulator.bluetooth.model;


import android.bluetooth.le.AdvertiseData;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

public class EddystoneURL extends Eddystone implements Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(EddystoneURL.class);

    private static final String DEFAULT_URL =  "http://www.alea.net/";

    private String url = DEFAULT_URL;
    private int power = -69;

    public EddystoneURL() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public AdvertiseData generateAdvertiseData() {
        byte txPower = (byte)power;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            byte[] compressedUrl;
            if (this.url == null || this.url.length() == 0) {
                compressedUrl = new byte[]{};
            }
            else {
                compressedUrl = UrlBeaconUrlCompressor.compress(this.url);
            }
            os.write(new byte[]{FRAME_TYPE_URL, txPower});
            os.write(compressedUrl);
        }
        catch(MalformedURLException e) {
            sLogger.warn("Error while generating ADData", e);
            return null;
        }
        catch (IOException e) {
            sLogger.warn("Error while generating ADData", e);
            return null;
        }

        byte[] serviceData = os.toByteArray();

        final ParcelUuid parcelUuid = ParcelUuid.fromString(Eddystone.EDDYSTONE_SERVICE_UUID);

        return new AdvertiseData.Builder()
                .addServiceUuid(parcelUuid)
                .addServiceData(parcelUuid, serviceData)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeInt(this.power);
    }

    protected EddystoneURL(Parcel in) {
        this.url = in.readString();
        this.power = in.readInt();
    }

    public static final Parcelable.Creator<EddystoneURL> CREATOR = new Parcelable.Creator<EddystoneURL>() {
        @Override
        public EddystoneURL createFromParcel(Parcel source) {
            return new EddystoneURL(source);
        }

        @Override
        public EddystoneURL[] newArray(int size) {
            return new EddystoneURL[size];
        }
    };
}

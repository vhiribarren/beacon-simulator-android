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

import net.alea.beaconsimulator.bluetooth.ByteTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class EddystoneUID extends Eddystone implements Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(EddystoneUID.class);

    private String namespace;
    private String instance;
    private int power;

    public EddystoneUID() {
        this.namespace = generateUidNamespace();
        this.instance = "000000000000";
        this.power = -65;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public static String generateUidNamespace() {
        String randomUUID = UUID.randomUUID().toString();
        return randomUUID.subSequence(0, 8)+randomUUID.substring(24, 36);
    }

    @Override
    public AdvertiseData generateAdvertiseData() {
        byte txPower = (byte)power;
        byte[] namespaceBytes = ByteTools.toByteArray(namespace);
        byte[] instanceBytes = ByteTools.toByteArray(instance);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(new byte[]{FRAME_TYPE_UID, txPower});
            os.write(namespaceBytes);
            os.write(instanceBytes);
        } catch (IOException e) {
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
        dest.writeString(this.namespace);
        dest.writeString(this.instance);
        dest.writeInt(this.power);
    }

    protected EddystoneUID(Parcel in) {
        this.namespace = in.readString();
        this.instance = in.readString();
        this.power = in.readInt();
    }

    public static final Parcelable.Creator<EddystoneUID> CREATOR = new Parcelable.Creator<EddystoneUID>() {
        @Override
        public EddystoneUID createFromParcel(Parcel source) {
            return new EddystoneUID(source);
        }

        @Override
        public EddystoneUID[] newArray(int size) {
            return new EddystoneUID[size];
        }
    };
}

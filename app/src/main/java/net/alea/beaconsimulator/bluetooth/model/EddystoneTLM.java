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

import java.nio.ByteBuffer;

public class EddystoneTLM extends Eddystone implements Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(EddystoneTLM.class);

    private int voltage;
    private double temperature;
    private long advertisingCount;
    private long uptime;

    public EddystoneTLM() {
        this.voltage = 0;
        this.temperature = -128.F;
        this.advertisingCount = 0;
        this.uptime = 0;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getAdvertisingCount() {
        return advertisingCount;
    }

    public void setAdvertisingCount(long advertisingCount) {
        this.advertisingCount = advertisingCount;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    @Override
    public AdvertiseData generateAdvertiseData() {

        final ParcelUuid parcelUuid = ParcelUuid.fromString(Eddystone.EDDYSTONE_SERVICE_UUID);
        final ByteBuffer buffer = ByteBuffer.allocate(14);

        buffer.put(FRAME_TYPE_TLM);
        buffer.put((byte)0x00); // version
        buffer.putShort((short)voltage);
        buffer.put(ByteTools.convertDoubleToFixPoint(temperature));
        buffer.putInt((int)advertisingCount);
        buffer.putInt((int)uptime);

        return new AdvertiseData.Builder()
                .addServiceUuid(parcelUuid)
                .addServiceData(parcelUuid, buffer.array())
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
        dest.writeInt(this.voltage);
        dest.writeDouble(this.temperature);
        dest.writeLong(this.advertisingCount);
        dest.writeLong(this.uptime);
    }

    protected EddystoneTLM(Parcel in) {
        this.voltage = in.readInt();
        this.temperature = in.readDouble();
        this.advertisingCount = in.readLong();
        this.uptime = in.readLong();
    }

    public static final Creator<EddystoneTLM> CREATOR = new Creator<EddystoneTLM>() {
        @Override
        public EddystoneTLM createFromParcel(Parcel source) {
            return new EddystoneTLM(source);
        }

        @Override
        public EddystoneTLM[] newArray(int size) {
            return new EddystoneTLM[size];
        }
    };
}

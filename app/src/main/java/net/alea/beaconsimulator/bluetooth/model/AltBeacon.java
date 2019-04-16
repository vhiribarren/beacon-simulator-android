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
import android.bluetooth.le.ScanRecord;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator;
import net.alea.beaconsimulator.bluetooth.ByteTools;

import java.nio.ByteBuffer;
import java.util.UUID;

public class AltBeacon implements AdvertiseDataGenerator, Parcelable {

    public final static short BEACON_CODE = (short)0xBEAC;
    public final static int MANUFACTURER_PACKET_SIZE = 24;

    private int manufacturerId;
    private UUID beaconNamespace;
    private int major;
    private int minor;
    private String manufacturerReserved;
    private byte power;


    public AltBeacon() {
        setManufacturerReserved("00");
        setManufacturerId(0xFFFF);
        setBeaconNamespace(UUID.randomUUID());
        setMajor(0);
        setMinor(0);
        setPower((byte)-69);
    }

    public UUID getBeaconNamespace() {
        return beaconNamespace;
    }

    public void setBeaconNamespace(UUID beaconNamespace) {
        this.beaconNamespace = beaconNamespace;
    }

    public int getMajor() {
        return major;
    }

    public final void setMajor(int major) {
        this.major = ByteTools.capToUnsignedShort(major);
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = ByteTools.capToUnsignedShort(minor);
    }

    public int getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(int manufacturerId) {
        this.manufacturerId = ByteTools.capToUnsignedShort(manufacturerId);
    }

    public String getManufacturerReserved() {
        return manufacturerReserved;
    }

    public void setManufacturerReserved(String manufacturerReserved) {
        if (manufacturerReserved.length() > 2) {
            this.manufacturerReserved = "00";
            return;
        }
        this.manufacturerReserved = manufacturerReserved;
    }

    public byte getPower() {
        return power;
    }

    public void setPower(byte power) {
        this.power = power;
    }


    @Override
    public AdvertiseData generateAdvertiseData() {
        /* When manufacturer reserved value is greater than 127, it cannot be
        converted to a byte. Hence a first conversion to int, then a cast to remove
        excessive bits. */
        final byte manufacturerReserved = (byte)Integer.parseInt(getManufacturerReserved(), 16);
        final ByteBuffer buffer = ByteBuffer.allocate(MANUFACTURER_PACKET_SIZE);
        buffer.putShort(BEACON_CODE);
        buffer.putLong(getBeaconNamespace().getMostSignificantBits());
        buffer.putLong(getBeaconNamespace().getLeastSignificantBits());
        buffer.put(ByteTools.toShortInBytes_BE(getMajor()));
        buffer.put(ByteTools.toShortInBytes_BE(getMinor()));
        buffer.put(getPower());
        buffer.put(manufacturerReserved);
        return new AdvertiseData.Builder()
                .addManufacturerData(getManufacturerId(), buffer.array())
                .build();
    }


    public static AltBeacon parseRecord(ScanRecord scanRecord) {
        // Check data validity
        final SparseArray<byte[]> manufacturers = scanRecord.getManufacturerSpecificData();
        if (manufacturers == null || manufacturers.size() != 1) {
            return null;
        }
        final byte[] data = manufacturers.valueAt(0);
        if (data.length != MANUFACTURER_PACKET_SIZE) {
            return null;
        }
        final ByteBuffer buffer = ByteBuffer.wrap(data);
        final short beaconCode = buffer.getShort();
        if (beaconCode != BEACON_CODE) {
            return null;
        }
        // Parse data
        final long uuidHigh = buffer.getLong();
        final long uuidLow = buffer.getLong();
        final int major =  ByteTools.toIntFromShortInBytes_BE(new byte[]{buffer.get(), buffer.get()});
        final int minor = ByteTools.toIntFromShortInBytes_BE(new byte[]{buffer.get(), buffer.get()});
        final byte power = buffer.get();
        final byte manufacturerReserved = buffer.get();
        final AltBeacon altBeacon = new AltBeacon();
        altBeacon.setBeaconNamespace(new UUID(uuidHigh, uuidLow));
        altBeacon.setMajor(major);
        altBeacon.setMinor(minor);
        altBeacon.setPower(power);
        altBeacon.setManufacturerReserved(ByteTools.bytesToHex(new byte[]{manufacturerReserved}));
        return altBeacon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.manufacturerId);
        dest.writeSerializable(this.beaconNamespace);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeString(this.manufacturerReserved);
        dest.writeByte(this.power);
    }

    protected AltBeacon(Parcel in) {
        this.manufacturerId = in.readInt();
        this.beaconNamespace = (UUID) in.readSerializable();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.manufacturerReserved = in.readString();
        this.power = in.readByte();
    }

    public static final Parcelable.Creator<AltBeacon> CREATOR = new Parcelable.Creator<AltBeacon>() {
        @Override
        public AltBeacon createFromParcel(Parcel source) {
            return new AltBeacon(source);
        }

        @Override
        public AltBeacon[] newArray(int size) {
            return new AltBeacon[size];
        }
    };
}

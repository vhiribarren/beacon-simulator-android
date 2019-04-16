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

import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseData;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import net.alea.beaconsimulator.bluetooth.ByteTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EddystoneEID extends Eddystone implements Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(EddystoneEID.class);

    private int power;
    private String identityKey;
    private int counterOffset; // in seconds
    private byte rotationPeriodExponent;


    public EddystoneEID() {
        this.power = -65;
        this.identityKey = "00112233445566778899AABBCCDDEEFF";
        this.counterOffset = 0;
        this.rotationPeriodExponent = 5;
    }


    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }

    public int getCounterOffset() {
        return counterOffset;
    }

    public void setCounterOffset(int counterOffset) {
        this.counterOffset = counterOffset;
    }

    public byte getRotationPeriodExponent() {
        return rotationPeriodExponent;
    }

    public void setRotationPeriodExponent(byte rotationPeriodExponent) {
        this.rotationPeriodExponent = rotationPeriodExponent;
    }

    public long getBeaconCounter() {
        return new Date().getTime()/1000 + counterOffset;
    }

    public long getBeaconCountdown() {
        long counter = getBeaconCounter();
        long counterChangeMoment = (long)(((counter >> rotationPeriodExponent) << rotationPeriodExponent) + Math.pow(2, rotationPeriodExponent));
        return counterChangeMoment - counter;
    }

    public long getNextChangeTimestampMilliseconds() {
        long counter = getBeaconCounter();
        return (long)(((counter >> rotationPeriodExponent) << rotationPeriodExponent) + Math.pow(2, rotationPeriodExponent))*1000;
    }

    public byte[] generateEidIdentifier() throws GeneralSecurityException {
        return generateEidIdentifier(new Date());
    }

    public byte[] generateEidIdentifier(Date date) throws GeneralSecurityException {
        // Get beacon counter
        final int counter = (int)(date.getTime()/1000 + counterOffset);
        // Get shared key
        final byte[] identityKey = ByteTools.toByteArray(getIdentityKey());
        // Compute temporary key
        byte[] temporaryKeyBase = {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // padding
                (byte)0xFF, // salt
                0x00, 0x00, // padding
                (byte)(counter >> 24), (byte)(counter >> 16) // top 16 bits of counter
        };
        @SuppressLint("GetInstance")
        Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(identityKey, "AES"));
        byte[] temporaryKey = aes.doFinal(temporaryKeyBase);
        // Encrypt challenge
        int trunkedCounter = (counter >> rotationPeriodExponent) << rotationPeriodExponent;
        byte[] eidBase = {
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // padding
                rotationPeriodExponent, // rotation period exponent
                (byte)(trunkedCounter >> 24), (byte)(trunkedCounter >> 16), (byte)(trunkedCounter >> 8), (byte)trunkedCounter
        };
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(temporaryKey, "AES"));
        byte[] fullKey = aes.doFinal(eidBase);
        return Arrays.copyOf(fullKey, 8) ;
    }

    @Override
    public AdvertiseData generateAdvertiseData() {

        final ParcelUuid parcelUuid = ParcelUuid.fromString(Eddystone.EDDYSTONE_SERVICE_UUID);
        final ByteBuffer buffer = ByteBuffer.allocate(14);
        final byte[] eidIdentifier;
        try {
            eidIdentifier = generateEidIdentifier();
        }
        catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        buffer.put(FRAME_TYPE_EID);
        buffer.put((byte)power); // version
        buffer.put(eidIdentifier);

        return new AdvertiseData.Builder()
                .addServiceUuid(parcelUuid)
                .addServiceData(parcelUuid, buffer.array())
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build();
    }

    public static String generateRandomIdentityKey() {
        StringBuilder builder = new StringBuilder(32);
        for(int pos=0; pos<32; pos++) {
            builder.append(Integer.toHexString((int)(Math.random()*15)));
        }
        return builder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.power);
        dest.writeString(this.identityKey);
        dest.writeInt(this.counterOffset);
        dest.writeByte(this.rotationPeriodExponent);
    }

    protected EddystoneEID(Parcel in) {
        this.power = in.readInt();
        this.identityKey = in.readString();
        this.counterOffset = in.readInt();
        this.rotationPeriodExponent = in.readByte();
    }

    public static final Creator<EddystoneEID> CREATOR = new Creator<EddystoneEID>() {
        @Override
        public EddystoneEID createFromParcel(Parcel source) {
            return new EddystoneEID(source);
        }

        @Override
        public EddystoneEID[] newArray(int size) {
            return new EddystoneEID[size];
        }
    };
}

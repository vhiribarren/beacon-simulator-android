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

import android.bluetooth.le.AdvertiseSettings;
import android.os.Parcel;
import android.os.Parcelable;

public class Settings implements Parcelable {

    private boolean connectable;
    private PowerLevel powerLevel;
    private AdvertiseMode advertiseMode;

    public Settings() {
        this.connectable = false;
        this.powerLevel = PowerLevel.medium;
        this.advertiseMode = AdvertiseMode.balanced;
    }


    public boolean isConnectable() {
        return connectable;
    }

    public void setConnectable(boolean connectable) {
        this.connectable = connectable;
    }

    public PowerLevel getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(PowerLevel powerLevel) {
        this.powerLevel = powerLevel;
    }

    public AdvertiseMode getAdvertiseMode() {
        return advertiseMode;
    }

    public void setAdvertiseMode(AdvertiseMode advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    public AdvertiseSettings generateADSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setConnectable(connectable);
        switch (powerLevel) {
            case high:
                builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
                break;
            case medium:
                builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
                break;
            case low:
                builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
                break;
            case ultraLow:
            default:
                builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
                break;
        }
        switch (advertiseMode) {
            case lowLatency:
                builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
                break;
            case balanced:
                builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
                break;
            case lowPower:
            default:
                builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
                break;
        }
        return builder.build();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.connectable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.powerLevel == null ? -1 : this.powerLevel.ordinal());
        dest.writeInt(this.advertiseMode == null ? -1 : this.advertiseMode.ordinal());
    }

    protected Settings(Parcel in) {
        this.connectable = in.readByte() != 0;
        int tmpPowerLevel = in.readInt();
        this.powerLevel = tmpPowerLevel == -1 ? null : PowerLevel.values()[tmpPowerLevel];
        int tmpAdvertiseMode = in.readInt();
        this.advertiseMode = tmpAdvertiseMode == -1 ? null : AdvertiseMode.values()[tmpAdvertiseMode];
    }

    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        @Override
        public Settings createFromParcel(Parcel source) {
            return new Settings(source);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
}

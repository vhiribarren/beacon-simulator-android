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
import android.os.Parcelable;

import net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator;
import net.alea.beaconsimulator.bluetooth.ByteTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

public class IBeacon implements AdvertiseDataGenerator, Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(IBeacon.class);

    private UUID proximityUUID;
    private int major;
    private int minor;
    private int power;

    public IBeacon() {
        this.proximityUUID = UUID.randomUUID();
        this.power = -65;
    }

    public IBeacon(UUID proximityUUID) {
        this.proximityUUID = proximityUUID;
    }

    public UUID getProximityUUID() {
        return proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPower() {
        return power;
    }

    public void setProximityUUID(UUID proximityUUID) { this.proximityUUID = proximityUUID; }

    public void setMajor(int major) {
        this.major = ByteTools.capToUnsignedShort(major);
    }

    public void setMinor(int minor) {
        this.minor = ByteTools.capToUnsignedShort(minor);
    }

    public void setPower(int power) {
        if (power < -128 || 127 < power) {
            this.power = 0;
        }
        else {
            this.power = power;
        }
    }

    @Override
    public AdvertiseData generateAdvertiseData() {
        throw new UnsupportedOperationException("Not implemented due to license issues");
        /*
        The support of iBeacon is removed from this open source code. But you can implement it
        while keeping the implementation close.

        The Apple iBeacon license states:

        > Licensee will not, without Apple's express prior written consent:
        >
        > (i) incorporate, combine, or distribute any Licensed Technology, or any derivative
        > thereof, with any Public Software,
        >
        > or
        >
        > (ii) use any Public Software in the development of Licensed Products,
        >
        > in such a way that would cause the Licensed Technology, or any derivative
        > thereof, to be subject to all or part of the license obligations or other intellectual
        > property related terms with respect to such Public Software. As used in this
        > subsection, "Public Software" means any software that, as a condition of use,
        > copying, modification or redistribution, (a) requires attribution, (b) requires such
        > software and derivative works thereof to be disclosed or distributed in source
        > code form, or (c) requires such software to be licensed for the purpose of making
        > derivative works, or to be redistributed free of charge, commonly referred to as
        > free or open source software, including but not limited to software licensed under
        > the GNU General Public License, Lesser/Library GPL, Affero GPL, Mozilla Public
        > License, Common Public License, Common Development and Distribution
        > License, Apache, MIT, or BSD license.

        In order to comply with those requirements, the iBeacon Bluetooth low energy advertise
        data packet format is removed from this source code.

        The code may still contains some strings or data format referring to it (e.g. UUID,
        major, and minor numbers) since there are part of public interfaces and knowledge
        for developers.

        This program is free software; you can redistribute it and/or modify it under
        the terms of the GNU General Public License as published by the Free Software
        Foundation; either version 3 of the License, or (at your option) any later
        version.

        Linking Beacon Simulator statically or dynamically with other modules is making
        a combined work based on Beacon Simulator. Thus, the terms and conditions of
        the GNU General Public License cover the whole combination.

        As a special exception, the copyright holders of Beacon Simulator give you
        permission to combine Beacon Simulator program with free software programs
        or libraries that are released under the GNU LGPL and with independent
        modules that communicate with Beacon Simulator solely through the
        net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator interface. You may
        copy and distribute such a system following the terms of the GNU GPL for
        Beacon Simulator and the licenses of the other code concerned, provided that
        you include the source code of that other code when and as the GNU GPL
        requires distribution of source code and provided that you do not modify the
        net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator interface.

        The intent of this license exception and interface is to allow Bluetooth low energy
        closed or proprietary advertise data packet structures and contents to be sensibly
        kept closed, while ensuring the GPL is applied. This is done by using an interface
        which only purpose is to generate android.bluetooth.le.AdvertiseData objects.

        This exception is an additional permission under section 7 of the GNU General
        Public License, version 3 (“GPLv3”).
        */
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.proximityUUID);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.power);
    }

    protected IBeacon(Parcel in) {
        this.proximityUUID = (UUID) in.readSerializable();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.power = in.readInt();
    }

    public static final Parcelable.Creator<IBeacon> CREATOR = new Parcelable.Creator<IBeacon>() {
        @Override
        public IBeacon createFromParcel(Parcel source) {
            return new IBeacon(source);
        }

        @Override
        public IBeacon[] newArray(int size) {
            return new IBeacon[size];
        }
    };
}

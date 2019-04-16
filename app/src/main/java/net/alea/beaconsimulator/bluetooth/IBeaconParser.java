package net.alea.beaconsimulator.bluetooth;

import android.bluetooth.le.ScanRecord;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.alea.beaconsimulator.bluetooth.model.IBeacon;

public class IBeaconParser implements AdvertiseDataParser<IBeacon> {

    @Nullable
    @Override
    public IBeacon parseScanRecord(@NonNull ScanRecord scanRecord) {
        return null;
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

}

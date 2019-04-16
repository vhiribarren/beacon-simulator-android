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

import android.bluetooth.le.ScanRecord;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * This interface is part of the exception allowing the combination of Beacon Simulator
 * with independent modules. Those independent modules can have their own license,
 * and their source can be kept close, as long as the exception is kept in the license
 * under section 7 of the GNU General Public License, version 3 (“GPLv3”). If this interface
 * is changed or if someone redistributed this code source to you without the exception clause,
 * implementation of this interface is covered by the GPLv3 and you have to redistribute
 * the source code of the implementation.
 *
 * The intent of this license exception and interface is to allow Bluetooth low energy
 * closed or proprietary advertise data packet structures and contents to be sensibly
 * kept closed, while ensuring the GPL is applied. This is done by using interfaces
 * which only purposes are to generate android.bluetooth.le.AdvertiseData objects or
 * to parse android.bluetooth.le.ScanResult having proprietary format to a usable
 * object.
 *
 * A usage that tries to circumvent the intent of this exception is not allowed.
 *
 * @param <T> the object type this interface can generate from the parseScanRecord method
 */
public interface AdvertiseDataParser<T> {

    /**
     * The only purpose of this method is to take a ScanResult and to create a model
     * representing the scanned device.
     *
     * There should not be any side effects.
     *
     * @param scanRecord the data to parse into an object representing the beacon
     * @return null if parse issue or if the scanned device is not compatible with this parser
     */
    @Nullable T parseScanRecord(@NonNull ScanRecord scanRecord);

}

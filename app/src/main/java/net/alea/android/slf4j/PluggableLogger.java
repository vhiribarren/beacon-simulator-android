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

package net.alea.android.slf4j;



import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;


public class PluggableLogger extends AbstractSimpleLogger {

    public static final String MARKER_SEND_CRASHLYTICS = "send_crashlytics";

    private final PluggableLoggerFactory mLogContext;
    private final Marker mMarkerSendCrashlytics = MarkerFactory.getMarker(MARKER_SEND_CRASHLYTICS);

    PluggableLogger(String name, PluggableLoggerFactory logContext) {
        super(name);
        mLogContext = logContext;
    }

    protected void log(Level level, Marker marker, String message, Throwable throwable) {
        final int levelInt = level.toInt();
        for(PluggableLoggerFactory.SortedLogger sortedLogger : mLogContext.getLoggers()) {
            if (levelInt < sortedLogger.level) {
                break;
            }
            sortedLogger.logger.log(level, name, marker, message, throwable);
        }
    }

    protected boolean isLoggable(Level level, Marker marker) {
        return mLogContext.getLoggers().size() != 0 && level.toInt() >= mLogContext.getLoggers().get(0).level;
    }




}
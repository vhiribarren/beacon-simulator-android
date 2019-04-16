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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;


abstract class AbstractSimpleLogger implements Logger {
    final protected String name;

    AbstractSimpleLogger(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isTraceEnabled() {
        return isLoggable(Level.TRACE, null);
    }

    public void trace(final String msg) {
        formatAndLog(Level.TRACE, null, name, msg);
    }

    public void trace(final String format, final Object param1) {
        formatAndLog(Level.TRACE, null, format, param1);
    }

    public void trace(final String format, final Object param1, final Object param2) {
        formatAndLog(Level.TRACE, null, format, param1, param2);
    }

    public void trace(final String format, final Object... arguments) {
        formatAndLog(Level.TRACE, null, format, arguments);
    }

    public void trace(final String msg, final Throwable t) {
        formatAndLog(Level.TRACE, null,msg, t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return isLoggable(Level.TRACE, marker);
    }

    public void trace(Marker marker, String msg) {
        formatAndLog(Level.TRACE, marker, msg);
    }

    public void trace(Marker marker, String format, Object arg) {
        formatAndLog(Level.TRACE, marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        formatAndLog(Level.TRACE, marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object... argArray) {
        formatAndLog(Level.TRACE, marker, format, argArray);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        formatAndLog(Level.TRACE, marker, msg, t);
    }

    public boolean isDebugEnabled() {
        return isLoggable(Level.DEBUG, null);
    }

    public void debug(final String msg) {
        formatAndLog(Level.DEBUG, null, msg);
    }

    public void debug(final String format, final Object arg1) {
        formatAndLog(Level.DEBUG, null, format, arg1);
    }

    public void debug(final String format, final Object param1, final Object param2) {
        formatAndLog(Level.DEBUG, null, format, param1, param2);
    }

    public void debug(final String format, final Object... arguments) {
        formatAndLog(Level.DEBUG, null, format, arguments);
    }

    public void debug(final String msg, final Throwable t) {
        formatAndLog(Level.DEBUG, null, msg, t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return isLoggable(Level.DEBUG, marker);
    }

    public void debug(Marker marker, String msg) {
        formatAndLog(Level.DEBUG, marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        formatAndLog(Level.DEBUG, marker, format, arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        formatAndLog(Level.DEBUG, marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        formatAndLog(Level.DEBUG, marker, format, arguments);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        formatAndLog(Level.DEBUG, marker, msg, t);
    }

    public boolean isInfoEnabled() {
        return isLoggable(Level.INFO, null);
    }

    public void info(final String msg) {
        formatAndLog(Level.INFO, null, msg);
    }

    public void info(final String format, final Object arg) {
        formatAndLog(Level.INFO, null, format, arg);
    }

    public void info(final String format, final Object arg1, final Object arg2) {
        formatAndLog(Level.INFO, null, format, arg1, arg2);
    }

    public void info(final String format, final Object... arguments) {
        formatAndLog(Level.INFO, null, format, arguments);
    }

    public void info(final String msg, final Throwable t) {
        formatAndLog(Level.INFO, null, msg, t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return isLoggable(Level.INFO, marker);
    }

    public void info(Marker marker, String msg) {
        formatAndLog(Level.INFO, marker, msg);
    }

    public void info(Marker marker, String format, Object arg) {
        formatAndLog(Level.INFO, marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        formatAndLog(Level.INFO, marker, format, arg1, arg2);
    }

    public void info(Marker marker, String format, Object... arguments) {
        formatAndLog(Level.INFO, marker, format, arguments);
    }

    public void info(Marker marker, String msg, Throwable t) {
        formatAndLog(Level.INFO, marker, msg, t);
    }


    public boolean isWarnEnabled() {
        return isLoggable(Level.WARN, null);
    }

    public void warn(final String msg) {
        formatAndLog(Level.WARN, null, name, msg);
    }

    public void warn(final String format, final Object param1) {
        formatAndLog(Level.WARN, null, format, param1);
    }

    public void warn(final String format, final Object param1, final Object param2) {
        formatAndLog(Level.WARN, null, format, param1, param2);
    }

    public void warn(final String format, final Object... arguments) {
        formatAndLog(Level.WARN, null, format, arguments);
    }

    public void warn(final String msg, final Throwable t) {
        formatAndLog(Level.WARN, null,msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return isLoggable(Level.WARN, marker);
    }

    public void warn(Marker marker, String msg) {
        formatAndLog(Level.WARN, marker, msg);
    }

    public void warn(Marker marker, String format, Object arg) {
        formatAndLog(Level.WARN, marker, format, arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        formatAndLog(Level.WARN, marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object... argArray) {
        formatAndLog(Level.WARN, marker, format, argArray);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        formatAndLog(Level.WARN, marker, msg, t);
    }

    public boolean isErrorEnabled() {
        return isLoggable(Level.ERROR, null);
    }

    public void error(final String msg) {
        formatAndLog(Level.ERROR, null, name, msg);
    }

    public void error(final String format, final Object param1) {
        formatAndLog(Level.ERROR, null, format, param1);
    }

    public void error(final String format, final Object param1, final Object param2) {
        formatAndLog(Level.ERROR, null, format, param1, param2);
    }

    public void error(final String format, final Object... arguments) {
        formatAndLog(Level.ERROR, null, format, arguments);
    }

    public void error(final String msg, final Throwable t) {
        formatAndLog(Level.ERROR, null,msg, t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return isLoggable(Level.ERROR, marker);
    }

    public void error(Marker marker, String msg) {
        formatAndLog(Level.ERROR, marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        formatAndLog(Level.ERROR, marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        formatAndLog(Level.ERROR, marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object... argArray) {
        formatAndLog(Level.ERROR, marker, format, argArray);
    }

    public void error(Marker marker, String msg, Throwable t) {
        formatAndLog(Level.ERROR, marker, msg, t);
    }

    private void formatAndLog(Level level, Marker marker, String format, Object... argArray) {
        if (isLoggable(level, marker)) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            log(level, marker, ft.getMessage(), ft.getThrowable());
        }
    }

    protected abstract boolean isLoggable(Level level, Marker marker);

    protected abstract void log(Level level, Marker marker, String message, Throwable throwable);

}
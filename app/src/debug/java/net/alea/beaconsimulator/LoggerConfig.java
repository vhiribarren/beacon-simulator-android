package net.alea.beaconsimulator;

import net.alea.android.slf4j.LoggerChannelLogcat;
import net.alea.android.slf4j.PluggableLoggerFactory;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;


public class LoggerConfig {

    public static void configLogger() {
        ((PluggableLoggerFactory) LoggerFactory.getILoggerFactory())
                .addLogger(new LoggerChannelLogcat(), Level.TRACE);
    }

}

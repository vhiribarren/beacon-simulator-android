package org.slf4j.impl;

import net.alea.android.slf4j.PluggableLoggerFactory;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;


public class StaticLoggerBinder implements LoggerFactoryBinder {

    /**
     * Declare the version of the SLF4J API this implementation is compiled
     * against. The value of this field is usually modified with each release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.24"; // !final

    private static final StaticLoggerBinder _instance = new StaticLoggerBinder();

    private final ILoggerFactory _loggerFactory;


    private StaticLoggerBinder() {
        _loggerFactory = new PluggableLoggerFactory();
    }

    public ILoggerFactory getLoggerFactory() {
        return _loggerFactory;
    }

    public String getLoggerFactoryClassStr() {
        return PluggableLoggerFactory.class.getName();
    }

    public static StaticLoggerBinder getSingleton() {
        return _instance;
    }
}
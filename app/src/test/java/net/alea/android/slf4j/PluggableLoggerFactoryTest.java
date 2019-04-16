package net.alea.android.slf4j;

import android.support.test.filters.SmallTest;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

import static org.mockito.Mockito.*;

@SmallTest
public class PluggableLoggerFactoryTest {

    public final static int NAME_MAX_SIZE = 23;

    @Test
    public void removeLoggers() {
        PluggableLoggerFactory factory = new PluggableLoggerFactory();
        assertThat(factory.getLoggers().size(), equalTo(0));
        factory.addLogger(mock(LoggerChannel.class), Level.TRACE);
        factory.addLogger(mock(LoggerChannel.class), Level.TRACE);
        assertThat(factory.getLoggers().size(), equalTo(2));
        factory.removeAll();
        assertThat(factory.getLoggers().size(), equalTo(0));
    }


    @Test
    public void loggerSortedFromLowToHighLevel() {
        // Fixture
        LoggerChannel loggerTrace = mock(LoggerChannel.class);
        LoggerChannel loggerInfo = mock(LoggerChannel.class);
        LoggerChannel loggerError = mock(LoggerChannel.class);
        PluggableLoggerFactory factory = new PluggableLoggerFactory();
        // Test
        factory.addLogger(loggerInfo, Level.INFO);
        factory.addLogger(loggerError, Level.ERROR);
        factory.addLogger(loggerTrace, Level.TRACE);
        // Assert
        assertThat(factory.getLoggers().get(0).logger, sameInstance(loggerTrace));
        assertThat(factory.getLoggers().get(1).logger, sameInstance(loggerInfo));
        assertThat(factory.getLoggers().get(2).logger, sameInstance(loggerError));
    }


    @Test
    public void loggingAccordingToLevel() {
        // Fixture
        LoggerChannel loggerTrace = mock(LoggerChannel.class);
        LoggerChannel loggerInfo = mock(LoggerChannel.class);
        LoggerChannel loggerError = mock(LoggerChannel.class);
        PluggableLoggerFactory factory = new PluggableLoggerFactory();
        factory.addLogger(loggerInfo, Level.INFO);
        factory.addLogger(loggerError, Level.ERROR);
        factory.addLogger(loggerTrace, Level.TRACE);
        Logger logger = factory.getLogger("test");
        // Test
        logger.error("test");
        logger.info("test");
        logger.trace("test");
        // Assert
        verify(loggerTrace, times(3)).log(Mockito.any(Level.class), anyString(), Mockito.any(Marker.class), anyString(), Mockito.any(Throwable.class));
        verify(loggerInfo, times(2)).log(Mockito.any(Level.class), anyString(), Mockito.any(Marker.class), anyString(), Mockito.any(Throwable.class));
        verify(loggerError, times(1)).log(Mockito.any(Level.class), anyString(), Mockito.any(Marker.class), anyString(), Mockito.any(Throwable.class));
    }


    @Test
    public void loggerNameLimitation() {
        PluggableLoggerFactory factory = new PluggableLoggerFactory();
        Logger logger = factory.getLogger("123456789012345678901234567890");
        assertThat(logger.getName().length(), lessThanOrEqualTo(NAME_MAX_SIZE));
    }


    @Test
    public void loggerNotUsedAfterRemoved() {
        // Fixture
        LoggerChannel loggerChannel = mock(LoggerChannel.class);
        PluggableLoggerFactory factory = new PluggableLoggerFactory();
        factory.addLogger(loggerChannel, Level.INFO);
        Logger logger = factory.getLogger("test");
        // Test and assert
        logger.info("test");
        verify(loggerChannel, times(1)).log(Mockito.any(Level.class), anyString(), Mockito.any(Marker.class), anyString(), Mockito.any(Throwable.class));
        factory.removeAll();
        logger.info("test");
        verify(loggerChannel, times(1)).log(Mockito.any(Level.class), anyString(), Mockito.any(Marker.class), anyString(), Mockito.any(Throwable.class));
    }


}

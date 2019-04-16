package net.alea.android.slf4j;

import android.support.test.filters.SmallTest;

import org.junit.Test;

import static org.mockito.Mockito.*;

@SmallTest
public class LoggerChannelLogcatTest {

    @Test
    public void nullLevelDoNotCrash() {
        LoggerChannelLogcat loggerChannel = spy(LoggerChannelLogcat.class);
        loggerChannel.log(null, "name", null, "hello", null);
    }

}

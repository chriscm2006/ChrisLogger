package com.chrismcmeeking.chrisloggerlibrary;

import junit.framework.Assert;

import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertEquals;

/**
 * Created by chrismcmeeking on 3/8/16.
 */
public class LoggerTestUtils extends Assert {


    private static ShadowLog.LogItem getTopLogItem() {
        return ShadowLog.getLogs().get(ShadowLog.getLogs().size() - 1);
    }

    public static void assertTopShadowLogMessage(Logger.LogLevel logLevel, final String tag, final String message) {

        ShadowLog.LogItem topLogItem = ShadowLog.getLogs().get(ShadowLog.getLogs().size() - 1);

        assertEquals("Log level not equal", logLevel.mAssociatedAndroidLevel, topLogItem.type);
        assertEquals("Log tag not equal", tag, topLogItem.tag);
        assertEquals("Log message not equal", message, topLogItem.msg);
    }

}

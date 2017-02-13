package com.chriscm.clog;

import junit.framework.Assert;

import org.robolectric.shadows.ShadowLog;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * Utilities used for testing.
 */
class LoggerTestUtils extends Assert {


    private static ShadowLog.LogItem getTopLogItem() {
        return ShadowLog.getLogs().get(ShadowLog.getLogs().size() - 1);
    }

    public static void assertTopShadowLogMessage(CLog.LogLevel logLevel, final String tag, final String message) {

        ShadowLog.LogItem topLogItem = ShadowLog.getLogs().get(ShadowLog.getLogs().size() - 1);

        assertEquals("Log level not equal", logLevel.mAssociatedAndroidLevel, topLogItem.type);
        assertEquals("Log tag not equal", tag, topLogItem.tag);
        assertEquals("Log message not equal", message, topLogItem.msg);
    }

}

package com.chriscm.clog;

import junit.framework.Assert;

import org.robolectric.shadows.ShadowLog;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * Utilities used for testing.
 */
class LoggerTestUtils extends Assert {

    static ShadowLog.LogItem getTopLogItem() {
        return ShadowLog.getLogs().get(ShadowLog.getLogs().size() - 1);
    }
}

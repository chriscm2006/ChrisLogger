package com.chriscm.clog;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.requests.ClassRequest;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * Tests that focus on the logger when it is running in debug mode.  This mode is very verbose
 * and supplies a lot of information to the user.  Include class and function calls in log
 * tags.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk=18)
public class LogDebugModeTests {
    private static final boolean DEBUG_MODE = true;
    private static final boolean RELEASE_MODE = !DEBUG_MODE;

    private final static String TAG = "MyDefaultTag";

    @Test public void logTagDebugMode() {
        CLog.initialize(TAG, DEBUG_MODE);

        CLog.i("");

        Assert.assertTrue(LoggerTestUtils.getTopLogItem().tag.contains(LogDebugModeTests.class.getSimpleName()));
    }

    @Test public void logTagReleaseMode() {
        CLog.initialize(TAG, RELEASE_MODE);

        CLog.i("");

        Assert.assertEquals(TAG, LoggerTestUtils.getTopLogItem().tag);
    }

    @Test public void logsHiddenInReleaseMode() {
        CLog.initialize(TAG, RELEASE_MODE);

        final int initialSize = ShadowLog.getLogs().size();

        CLog.v("");
        Assert.assertEquals(initialSize, ShadowLog.getLogs().size());

        CLog.d("");
        Assert.assertEquals(initialSize, ShadowLog.getLogs().size());

        CLog.i("");
        Assert.assertEquals(initialSize + 1, ShadowLog.getLogs().size());
    }

}

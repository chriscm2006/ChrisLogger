package com.chriscm.clog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.chriscm.clog.LoggerTestUtils.assertTopShadowLogMessage;
import static org.junit.Assert.assertEquals;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * Tests that focus on the logger when it is running in debug mode.  This mode is very verbose
 * and supplies a lot of information to the user.  Include class and function calls in log
 * tags.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class LogDebugModeTests {

    private static final boolean DEBUG_MODE = true;


    @BeforeClass
    public static void configureLogger() {
        CLog.initialize("DefaultTag", DEBUG_MODE);
        CLog.setIncludeFunctionNames(false);
    }

    final static String TAG = BuildConfig.DEBUG ? LogDebugModeTests.class.getSimpleName() : BuildConfig.APPLICATION_ID;

    @Test
    public void messagesRespondToReleaseMode() {
        CLog.v("Hi");
        assertTopShadowLogMessage(Logger.LogLevel.VERBOSE, TAG, "Hi");

        //These log levels should only change tags based on release mode
        CLog.w("Aloha");
        assertTopShadowLogMessage(Logger.LogLevel.WARN, TAG, "Aloha");

        CLog.e("Umm...");
        assertTopShadowLogMessage(Logger.LogLevel.ERROR, TAG, "Umm...");

        CLog.wtf("Good Bye");
        assertTopShadowLogMessage(Logger.LogLevel.ASSERT, TAG, "Good Bye");
    }

    @Test
    public void messagesEscalateToInfo() {
        CLog.getLogger(LogDebugModeTests.class).setIsImportant(true);

        CLog.v("A message");
        assertTopShadowLogMessage(Logger.LogLevel.INFO, TAG, "A message");

        CLog.d("Another message");
        assertTopShadowLogMessage(Logger.LogLevel.INFO, TAG, "Another message");

        CLog.getLogger(LogDebugModeTests.class).setIsImportant(false);

        CLog.d("Another message");
        assertTopShadowLogMessage(Logger.LogLevel.DEBUG, TAG, "Another message");

        CLog.v("A message");
        assertTopShadowLogMessage(Logger.LogLevel.VERBOSE, TAG, "A message");
    }
}

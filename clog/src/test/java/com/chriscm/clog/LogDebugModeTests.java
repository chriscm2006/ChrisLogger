package com.chriscm.clog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.chriscm.clog.LoggerTestUtils.assertTopShadowLogMessage;
import com.chriscm.clog.CLog.LogLevel;
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
    }

    private final static String TAG = BuildConfig.DEBUG ? LogDebugModeTests.class.getSimpleName() : BuildConfig.APPLICATION_ID;

    @Test
    public void messagesRespondToReleaseMode() {
        CLog.v("Hi");
        assertTopShadowLogMessage(LogLevel.VERBOSE, TAG, "Hi");

        //These log levels should only change tags based on release mode
        CLog.w("Aloha");
        assertTopShadowLogMessage(LogLevel.WARN, TAG, "Aloha");

        CLog.e("Umm...");
        assertTopShadowLogMessage(LogLevel.ERROR, TAG, "Umm...");

        CLog.wtf("Good Bye");
        assertTopShadowLogMessage(LogLevel.ASSERT, TAG, "Good Bye");
    }

    @Test
    public void messagesEscalateToInfo() {

        CLog.v("A message");
        assertTopShadowLogMessage(LogLevel.INFO, TAG, "A message");

        CLog.d("Another message");
        assertTopShadowLogMessage(LogLevel.INFO, TAG, "Another message");

        CLog.d("Another message");
        assertTopShadowLogMessage(LogLevel.DEBUG, TAG, "Another message");

        CLog.v("A message");
        assertTopShadowLogMessage(LogLevel.VERBOSE, TAG, "A message");
    }
}

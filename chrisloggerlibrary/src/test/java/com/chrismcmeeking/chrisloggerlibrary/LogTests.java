package com.chrismcmeeking.chrisloggerlibrary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static com.chrismcmeeking.chrisloggerlibrary.LoggerTestUtils.assertTopShadowLogMessage;
import static org.junit.Assert.assertEquals;

/**
 * Created by chrismcmeeking on 3/8/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class LogTests {

    final static String TAG = BuildConfig.DEBUG ? LogTests.class.getSimpleName() : BuildConfig.APPLICATION_ID;

    @Test
    public void messagesRespondToReleaseMode() {
        if (BuildConfig.DEBUG) {
            Log.v("Hi");
            assertTopShadowLogMessage(Logger.LogLevel.VERBOSE, TAG, "Hi");
        } else {
            final int numMessages = ShadowLog.getLogs().size();
            Log.v("");
            Log.v("");
            assertEquals(numMessages, ShadowLog.getLogs().size());
        }
        //These log levels should only change tags based on release mode
        Log.w("Aloha");
        assertTopShadowLogMessage(Logger.LogLevel.WARN, TAG, "Aloha");

        Log.e("Umm...");
        assertTopShadowLogMessage(Logger.LogLevel.ERROR, TAG, "Umm...");

        Log.wtf("Good Bye");
        assertTopShadowLogMessage(Logger.LogLevel.ASSERT, TAG, "Good Bye");
    }

    @Test
    public void messagesEscalateToInfo() {
        Log.getLogger(LogTests.class).setIsImportant(true);

        Log.v("A message");
        assertTopShadowLogMessage(Logger.LogLevel.INFO, TAG, "A message");

        Log.d("Another message");
        assertTopShadowLogMessage(Logger.LogLevel.INFO, TAG, "Another message");

        Log.getLogger(LogTests.class).setIsImportant(false);

        Log.d("Another message");
        assertTopShadowLogMessage(Logger.LogLevel.DEBUG, TAG, "Another message");

        Log.v("A message");
        assertTopShadowLogMessage(Logger.LogLevel.VERBOSE, TAG, "A message");
    }
}

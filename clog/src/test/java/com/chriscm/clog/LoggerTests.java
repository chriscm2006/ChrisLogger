package com.chriscm.clog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import com.chriscm.clog.CLog.LogLevel;
import com.chriscm.clog.CLog.Logger;

import static com.chriscm.clog.LoggerTestUtils.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class LoggerTests {

    Logger logger = new Logger();

    final static String mTag = BuildConfig.DEBUG ? LoggerTests.class.getSimpleName() : BuildConfig.APPLICATION_ID;

    @BeforeClass
    public static void configureLogger() {
        CLog.initialize("DefaultTag", true);
        CLog.setIncludeFunctionNames(false);
    }

    @Test
    public void messagesRespondToReleaseMode() {

        logger.v("Hi");
        assertTopShadowLogMessage(LogLevel.VERBOSE, mTag, "Hi");

        //These log levels should only change tags based on release mode
        logger.w("Aloha");
        assertTopShadowLogMessage(LogLevel.WARN, mTag, "Aloha");

        logger.e("Umm...");
        assertTopShadowLogMessage(LogLevel.ERROR, mTag, "Umm...");

        logger.wtf("Good Bye");
        assertTopShadowLogMessage(LogLevel.ASSERT, mTag, "Good Bye");

    }
}
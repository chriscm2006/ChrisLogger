package com.chriscm.clog;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

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
        assertTopShadowLogMessage(Logger.LogLevel.VERBOSE, mTag, "Hi");

        //These log levels should only change tags based on release mode
        logger.w("Aloha");
        assertTopShadowLogMessage(Logger.LogLevel.WARN, mTag, "Aloha");

        logger.e("Umm...");
        assertTopShadowLogMessage(Logger.LogLevel.ERROR, mTag, "Umm...");

        logger.wtf("Good Bye");
        assertTopShadowLogMessage(Logger.LogLevel.ASSERT, mTag, "Good Bye");

    }

    @Test
    public void messageTags() {
        logger.w("high");
        assertTopShadowLogMessage(Logger.LogLevel.WARN, mTag, "high");

        logger.setTagIncludeFunctionName(true);

        final String tagWithFunction = mTag + ".messageTags";
        logger.wtf("nope");
        assertTopShadowLogMessage(Logger.LogLevel.ASSERT, tagWithFunction, "nope");

        logger.setTagIncludeLineNumber(true);

        logger.e("another log");

        //The log tag may have to change a lot, but it's worth it to test this.
        final String tagWithLine = tagWithFunction + "/59";
        assertTopShadowLogMessage(Logger.LogLevel.ERROR, tagWithLine, "another log");
    }

}
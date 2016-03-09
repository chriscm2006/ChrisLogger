package com.chrismcmeeking.chrisloggerlibrary;

import java.util.HashMap;

import static com.chrismcmeeking.chrisloggerlibrary.Utils.*;

/**
 * Created by chrismcmeeking on 3/8/16.
 */
public class CLog {

    static String DEFAULT_LOG_TAG = null;

    static boolean RELEASE_MODE = true;
    static boolean DEBUG_MODE = false;

    public static void initialize(final String defaultTag, final boolean debugMode) {
        RELEASE_MODE = !debugMode;
        DEBUG_MODE = debugMode;
        DEFAULT_LOG_TAG = defaultTag;
    }

    static HashMap<Class <? extends Object>, Logger> mLoggers = new HashMap<>();

    static Logger mDefaultLogger = new Logger();

    public static void v(final String message) {
        println(message, Logger.LogLevel.VERBOSE);
    }

    public static void d(final String message) {
        println(message, Logger.LogLevel.DEBUG);
    }

    public static void i(final String message) {
        println(message, Logger.LogLevel.INFO);
    }

    public static void w(final String message) {
        println(message, Logger.LogLevel.WARN);
    }

    public static void e(final String message) {
        println(message, Logger.LogLevel.ERROR);
    }


    public static void wtf(final String message) {
        println(message, Logger.LogLevel.ASSERT);
    }

    public static void println(final String message, final Logger.LogLevel logLevel) {
        try {
            String className = getFirstStackTraceElementNotInPackage().getClassName();
            getLogger(Class.forName(className)).println(logLevel, message);
        } catch (Exception stackTraceElementNotFound) {
           mDefaultLogger.println(logLevel, message);
        }
    }

    public static Logger getLogger(Class <? extends Object> clazz) {
        if (!mLoggers.containsKey(clazz)) {
            mLoggers.put(clazz, new Logger());
        }

        return mLoggers.get(clazz);
    }

    /**
     * Sets the include funtion names tag for all active loggers.
     * Also sets the static default value so any future constructed
     * loggers will have the same value for this tag.
     * @param value Set to true to include the function name in log tags
     */
    public static void setIncludeFunctionNames(final boolean value) {
        for (Logger logger : mLoggers.values()) {
            logger.setTagIncludeFunctionName(value);
        }

        Logger.DEFAULT_INCLUDE_FUNCTION_NAME = value;
    }

    /**
     * Sets the include line number tag for all active loggers.
     * Also sets the static default value so any future constructed
     * loggers will have the same value for this tag.
     * @param value Set to true to include line numbers in log tags.
     */
    public static void setIncludeLineNumber(final boolean value) {
        for (Logger logger : mLoggers.values()) {
            logger.setTagIncludeLineNumber(value);
        }

        Logger.DEFAULT_INCLUDE_LINE_NUMBER = value;
    }
}

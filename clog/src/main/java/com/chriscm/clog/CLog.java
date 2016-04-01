package com.chriscm.clog;

import android.util.Log;

import java.util.HashMap;

import static com.chriscm.clog.Utils.*;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * A static class API that can be used rather than creating your own logger instances.
 */
public class CLog {

    static String DEFAULT_LOG_TAG = null;
    static boolean RELEASE_MODE = true;
    static boolean DEBUG_MODE = false;
    static boolean isInitialized = false;

    /**
     * The logger must be initialized before any use.  This allows for customizing behavior
     * between release and debug modes, without actually changing your build configuration.
     * Making for easier testing, and forcing release mode if you wish.
     * @param defaultTag The flag that will be used when in release mode for all loggers.
     * @param debugMode  True if you're in debug mode.  Just use BuildConfig.DEBUG.
     */
    public static void initialize(final String defaultTag, final boolean debugMode) {

        //If a project is using Chris Logger and a library within is using ChrisLogger,
        //we don't want the Library to override the initialization.  So we only accept the first one.
        //Todo: Make Clog logging dependent on package instead.
        if (isInitialized) return;


        RELEASE_MODE = !debugMode;
        DEBUG_MODE = debugMode;
        DEFAULT_LOG_TAG = defaultTag;

        String mode = debugMode ? "debug" : "release";

        Log.i(defaultTag, "CLog initialized and in " + mode + " mode.");
    }

    static HashMap<Class <?>, Logger> mLoggers = new HashMap<>();

    static Logger mDefaultLogger = new Logger();

    /**
     * Convenience method, log to verbose log level.  If escalated, will log to info.
     * @param message The message to be logged.
     */
    public static void v(final String message) {
        println(message, Logger.LogLevel.VERBOSE);
    }

    /**
     * Convenience method, log to debug level.  If escalated, will log to info.
     * @param message The message to be logged.
     */
    public static void d(final String message) {
        println(message, Logger.LogLevel.DEBUG);
    }

    /**
     * Convenience method, log to info level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void i(final String message) {
        println(message, Logger.LogLevel.INFO);
    }

    /**
     * Convenience method, log to warning log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void w(final String message) {
        println(message, Logger.LogLevel.WARN);
    }

    /**
     * Convenience method, log to error log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void e(final String message) {
        println(message, Logger.LogLevel.ERROR);
    }

    /**
     * Convenience method, log to assert log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void wtf(final String message) {
        println(message, Logger.LogLevel.ASSERT);
    }

    /**
     * Prints the given message to the given log level.  The input level may not be the
     * same as the output level.  Messages below the info level can be escalated up to
     * the info log level.
     * @param message The message to be logged.
     * @param logLevel The target log level.
     */
    public static void println(final String message, final Logger.LogLevel logLevel) {
        try {
            String className = getFirstStackTraceElementNotInPackage().getClassName();
            getLogger(Class.forName(className)).println(logLevel, message);
        } catch (Exception stackTraceElementNotFound) {
           mDefaultLogger.println(logLevel, message);
        }
    }

    /**
     * Get the logger for a class object.  Not all Logger instance methods have a
     * static convenience method.  So this is required for some actions.
     * @param clazz The class you want the logger for.
     * @return The logger associated with that class.
     */
    public static Logger getLogger(Class <?> clazz) {
        if (!mLoggers.containsKey(clazz)) {
            mLoggers.put(clazz, new Logger());
        }

        return mLoggers.get(clazz);
    }

    /**
     * Sets the include function names tag for all active loggers.
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

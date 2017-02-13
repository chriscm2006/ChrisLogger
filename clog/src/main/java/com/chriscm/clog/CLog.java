package com.chriscm.clog;

import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * A static class API that can be used rather than creating your own logger instances.
 */
@SuppressWarnings("SameParameterValue")
public class CLog {

    private static String DEFAULT_LOG_TAG = "CLog";
    private static boolean DEBUG_MODE = false;
    private static boolean isInitialized = false;

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

        isInitialized = true;

        DEBUG_MODE = debugMode;
        DEFAULT_LOG_TAG = defaultTag;

        String mode = debugMode ? "debug" : "release";

        Log.i(defaultTag, "CLog initialized and in " + mode + " mode.");
    }

    private static final HashMap<Class <?>, Logger> mLoggers = new HashMap<>();

    private static final Logger mDefaultLogger = new Logger();

    /**
     * Convenience method, log to verbose log level.  If escalated, will log to info.
     * @param message The message to be logged.
     */
    public static void v(final String message) {
        println(message, LogLevel.VERBOSE);
    }

    /**
     * Convenience method, log to debug level.  If escalated, will log to info.
     * @param message The message to be logged.
     */
    public static void d(final String message) {
        println(message, LogLevel.DEBUG);
    }

    /**
     * Convenience method, log to info level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void i(final String message) {
        println(message, LogLevel.INFO);
    }

    /**
     * Convenience method, log to warning log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void w(final String message) {
        println(message, LogLevel.WARN);
    }

    /**
     * Convenience method, log to error log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void e(final String message) {
        println(message, LogLevel.ERROR);
    }

    /**
     * Convenience method, log to assert log level.  Doesn't escalate.
     * @param message The message to be logged.
     */
    public static void wtf(final String message) {
        println(message, LogLevel.ASSERT);
    }

    /**
     * Prints the given message to the given log level.  The input level may not be the
     * same as the output level.  Messages below the info level can be escalated up to
     * the info log level.
     * @param message The message to be logged.
     * @param logLevel The target log level.
     */
    private static void println(final String message, final LogLevel logLevel) {
        try {
            String className = getFirstStackTraceElementNotInPackage().getClassName();
            getLogger(Class.forName(className)).println(logLevel, message);
        } catch (Exception stackTraceElementNotFound) {
           mDefaultLogger.println(logLevel, message);
        }
    }

    /**
     * Convenience method to get a logger with a classname string.
     * @param className The name of the class you want the logger for.
     * @return The logger associated with that class.
     */
    public static Logger getLogger(final String className) {
        try {
            return getLogger(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return mDefaultLogger;
        }
    }
    /**
     * Get the logger for a class object.  Not all Logger instance methods have a
     * static convenience method.  So this is required for some actions.
     * @param clazz The class you want the logger for.
     * @return The logger associated with that class.
     */
    private static Logger getLogger(Class<?> clazz) {

        while (clazz.isMemberClass()) {
            clazz = clazz.getEnclosingClass();
        }

        if (!mLoggers.containsKey(clazz)) {
            mLoggers.put(clazz, new Logger());
        }

        return mLoggers.get(clazz);
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

    public enum LogLevel {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT);

        final int mAssociatedAndroidLevel;
        LogLevel(int androidLevel) {
            mAssociatedAndroidLevel = androidLevel;
        }
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    public static class Logger {

        static final boolean DEFAULT_INCLUDE_FUNCTION_NAME = true;

        static boolean DEFAULT_INCLUDE_LINE_NUMBER = false;

        //Don't log any messages less than this level.
        private LogLevel mLogLevel;

        //Include the function name as part of the log tag.  Decreases performance.
        private final boolean mTagIncludeFunctionName = DEFAULT_INCLUDE_FUNCTION_NAME;

        //Include the line number as part of the log tag.  Decreases performance.
        private boolean mTagIncludeLineNumber = DEFAULT_INCLUDE_LINE_NUMBER;

        /**
         * Convenience constructor, log everything.
         */
        public Logger() {
            this(LogLevel.VERBOSE);
        }

        /**
         * Construct a logger that filters levels below the given level.  Any logs below
         * this level will be ignored.
         * @param logLevel The log level.
         */
        public Logger(LogLevel logLevel) {
            mLogLevel = logLevel;
        }

        /**
         * For large code bases, knowing what line number a log came from can be handy.
         * @param includeLineNumber If true, line numbers will be included in log tags.
         * @return this, for command chaining.
         */
        public Logger setTagIncludeLineNumber(boolean includeLineNumber) {
            mTagIncludeLineNumber = includeLineNumber && CLog.DEBUG_MODE;
            return this;
        }

        private String getLogTag(final boolean includeLineNumber) {

            if (CLog.DEBUG_MODE) {

                final StackTraceElement functionCall;

                try {
                    functionCall = getFirstStackTraceElementNotInPackage();
                } catch (StackTraceElementNotFound stackTraceElementNotFound) {
                    stackTraceElementNotFound.printStackTrace();
                    return DEFAULT_LOG_TAG;
                }

                final String fileName = functionCall.getFileName();

                if (includeLineNumber) {
                    return "(" + fileName + ":" + functionCall.getLineNumber() + ")/";
                } else {
                    final int lastPeriodPosition = fileName.lastIndexOf('.');

                    return fileName.substring(0, lastPeriodPosition);
                }
            } else {
                return DEFAULT_LOG_TAG;
            }
        }

        /**
         * Convenience method, calculates "releaseMode" flag based on log level.  Only messages greater than
         * INFO level will be logged in release.
         * @param logLevel The level of teh given log message.
         * @param message The message to be logged.
         */
        public void println(LogLevel logLevel, final String message) {
            Log.println(logLevel.mAssociatedAndroidLevel, getLogTag(mTagIncludeLineNumber), message);
        }

        /**
         * Log to VERBOSE level.  Don't log in release mode.
         * @param message The message to be logged.
         */
        public void v(String message) {
            println(LogLevel.VERBOSE, message);
        }

        /**
         * Log to VERBOSE level.  Forced log in release mode.
         * @param message The message to be logged.
         */
        public void v_always(final String message) {
            println(LogLevel.VERBOSE, message);
        }

        /**
         * Log to DEBUG level.  Don't log in release mode.
         * @param message The message to be logged.
         */
        public void d(String message) {
            println(LogLevel.DEBUG, message);
        }

        /**
         * Log to DEBUG level.  Forced log in release mode.
         * @param message The message to be logged.
         */
        public void d_always(String message) {
            println(LogLevel.DEBUG, message);
        }

        /**
         * Log to WARN level.  Don't log in release mode.
         * @param message The message to be logged.
         */
        public void w(String message) {
            println(LogLevel.WARN, message);
        }

        /**
         * Log to ERROR level.  Don't log in release mode.
         * @param message The message to be logged.
         */
        public void e(String message) {
            println(LogLevel.ERROR, message);
        }

        /**
         * Log to ASSERT level.  Don't log in release mode.
         * @param message The message to be logged.
         */
        public void wtf(String message) {
            println(LogLevel.ASSERT, message);
        }

        /**
         * Modify the log level to temporarily change the level of messages that get filtered out.
         * Useful for debugging a specific function at a more verbose level.
         * @param logLevel The level to chane it to.
         */
        public void setLogLevel(LogLevel logLevel) {
            mLogLevel = logLevel;
        }
    }

    private static final List<Class<?>> CLASSES_IN_PACKAGE = Arrays.asList(CLog.class, Logger.class);

    private static boolean isClassInPackage(final Class <?> argClazz) {

        for (Class<?> clazz : CLASSES_IN_PACKAGE) {
            if (clazz == argClazz) return true;
        }

        return false;
    }

    public static class StackTraceElementNotFound extends Exception {
        StackTraceElementNotFound(final String message) {
            super(message);
        }
    }
    /*
    Find the first StackTraceElement that is not associated with the Logger class.
    This should be the element that initiated any logging action.
     */
    private static StackTraceElement getFirstStackTraceElementNotInPackage() throws StackTraceElementNotFound {

        boolean elementInClassFound = false;

        //We want to search through all elements, until we find one associated with this class
        //Then grab the first one not associated with this class.
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {

            final String callingClassName = element.getClassName();
            final Class<?> clazz;
            try {
                clazz = Class.forName(callingClassName);
            } catch (ClassNotFoundException e) {
                throw new StackTraceElementNotFound(e.getLocalizedMessage());
            }

            if (isClassInPackage(clazz)) {
                elementInClassFound = true;
            } else {
                if (elementInClassFound) {
                    return element;
                }
            }
        }

        throw new StackTraceElementNotFound("Went through entire loop without finding calling element.");
    }

}

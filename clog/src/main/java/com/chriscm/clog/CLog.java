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
public class CLog {

    static String DEFAULT_LOG_TAG = null;
    static boolean RELEASE_MODE = true;
    static boolean DEBUG_MODE = false;

    /**
     * The logger must be initialized before any use.  This allows for customizing behavior
     * between release and debug modes, without actually changing your build configuration.
     * Making for easier testing, and forcing release mode if you wish.
     * @param defaultTag The flag that will be used when in release mode for all loggers.
     * @param debugMode  True if you're in debug mode.  Just use BuildConfig.DEBUG.
     */
    public static void initialize(final String defaultTag, final boolean debugMode) {
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
    public static void println(final String message, final LogLevel logLevel) {
        try {
            String className = getFirstStackTraceElementNotInPackage().getClassName();
            Class clazz = Class.forName(className);

            while (clazz.isMemberClass()) {
                clazz = clazz.getEnclosingClass();
            }

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

    public static class Logger {

        static boolean DEFAULT_INCLUDE_FUNCTION_NAME = true;

        static boolean DEFAULT_INCLUDE_LINE_NUMBER = false;

        //The tag to send do the Android Logger.  You can filter by this string.
        private static final String DEFAULT_LOG_TAG = BuildConfig.APPLICATION_ID;

        //Don't log any messages less than this level.
        private LogLevel mLogLevel;

        //Include the function name as part of the log tag.  Decreases performance.
        private boolean mTagIncludeFunctionName = DEFAULT_INCLUDE_FUNCTION_NAME;

        //Include the line number as part of the log tag.  Decreases performance.
        private boolean mTagIncludeLineNumber = DEFAULT_INCLUDE_LINE_NUMBER;


        //Escalate all verbose and debug outputs to info.  This should be the only way
        //a log gets output on the info channel.
        private boolean mImportant = false;

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


    /*
    Knowing what function a logging statement originates from is valuable both for debugging
    purpose, and for tracking down errant logging statements that are no longer needed.
     */

        /**
         * Knowing what function a logging statement originates from is valuable for debugging.
         * @param includeFunctionName If true, function names will be included in log tags.
         * @return this, for command chaining.
         */
        public Logger setTagIncludeFunctionName(boolean includeFunctionName) {
            //Don't log function names in release mode, it's slow.
            mTagIncludeFunctionName = includeFunctionName && CLog.DEBUG_MODE;
            return this;
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

        /**
         * @param important Set whether or not this logger is in elevated status.
         * @return This command can be chained.
         *
         * "Important" logger instances have all of there verbose and debug logs elevated to "info"
         * so they can be seen easily.
         */
        @SuppressWarnings("unused")
        public Logger setIsImportant(boolean important) {
            mImportant = important;
            return this;
        }

        private String getLogTag(final boolean includeFunction, final boolean includeLineNumber) {

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
                    return fileName + ":" + functionCall.getLineNumber();
                } else {
                    final int lastPeriodPosition = fileName.lastIndexOf('.');

                    return fileName.substring(0, lastPeriodPosition);
                }
            } else {
                return DEFAULT_LOG_TAG;
            }
        }

        LogLevel calculateActualLogLevel(final LogLevel logLevel) {
            //Sometimes we want to bring focus to a certain logger.
            if (mImportant && CLog.DEBUG_MODE && logLevel.ordinal() < LogLevel.INFO.ordinal()) return LogLevel.INFO;

            return logLevel;
        }

        /**
         * Convenience method, calculates "releaseMode" flag based on log level.  Only messages greater than
         * INFO level will be logged in release.
         * @param logLevel The level of teh given log message.
         * @param message The message to be logged.
         */
        public void println(LogLevel logLevel, final String message) {
            final boolean logInReleaseMode = logLevel.ordinal() >= LogLevel.INFO.ordinal();
            println(logLevel, message, logInReleaseMode);
        }

        /**
         * Log a message to LogCat.  If log in release mode is set to false, message will not appear unless
         * the logger is initialized to debug mode.
         * @param logLevel The level of the given log message.
         * @param message The message to be logged.
         * @param logInReleaseMode Whether or not we want to see the mssage in release mode.
         */
        public void println(LogLevel logLevel, final String message, final boolean logInReleaseMode) {

            if (CLog.DEFAULT_LOG_TAG == null) {
                throw new RuntimeException("Must initialize logger library before use.");
            }

            //Only some messages get logged in release mode.
            if (CLog.RELEASE_MODE && !logInReleaseMode) return;

            //Some log levels are completely ignored in release mode.
            if (logLevel.ordinal() < LogLevel.INFO.ordinal() && CLog.RELEASE_MODE) return;

            if (logLevel.ordinal() < mLogLevel.ordinal()) return;

            Log.println(calculateActualLogLevel(logLevel).mAssociatedAndroidLevel, getLogTag(mTagIncludeFunctionName, mTagIncludeLineNumber), message);
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
            println(LogLevel.VERBOSE, message, true);
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
            println(LogLevel.DEBUG, message, true);
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

    private static List<Class<?>> CLASSES_IN_PACKAGE = Arrays.asList(CLog.class, Logger.class);

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
    public static StackTraceElement getFirstStackTraceElementNotInPackage() throws StackTraceElementNotFound {

        final String className = Logger.class.getName();
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

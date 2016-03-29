package com.chriscm.clog;

import android.util.Log;

import static com.chriscm.clog.Utils.*;

public class Logger {

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

            final String className = functionCall.getClassName();

            final int lastPeriodPosition = className.lastIndexOf('.');

            String result = className.substring(lastPeriodPosition + 1);

            if (includeFunction) {
                result += "." + functionCall.getMethodName();
            }

            if (includeLineNumber) {
                result += "(" + functionCall.getFileName() + ":" + functionCall.getLineNumber() + ")";
            }

            return result;
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


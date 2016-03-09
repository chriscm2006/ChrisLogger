package com.chrismcmeeking.chrisloggerlibrary;

import android.util.Log;

import static com.chrismcmeeking.chrisloggerlibrary.Utils.*;

class Logger {

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

    //The tag to send do the Android Logger.  You can filter by this string.
    private static final String DEFAULT_LOG_TAG = BuildConfig.APPLICATION_ID;

    //Don't log any messages less than this level.
    private LogLevel mLogLevel;

    //Include the function name as part of the log tag.  Decreases performance.
    private boolean mTagIncludeFunctionName = false;

    //Include the line number as part of the log tag.  Decreases performance.
    private boolean mTagIncludeLineNumber = false;

    //Escalate all verbose and debug outputs to info.  This should be the only way
    //a log gets output on the info channel.
    private boolean mImportant = false;

    //Convenience constructor, log everything.
    public Logger() {
        this(LogLevel.VERBOSE);
    }

    public Logger(LogLevel logLevel) {
        mLogLevel = logLevel;
    }


    /*
    Knowing what function a logging statement originates from is valuable both for debugging
    purpose, and for tracking down errant logging statements that are no longer needed.
     */
    public Logger setTagIncludeFunctionName(boolean includeFunctionName) {
        //Don't log function names in release mode, it's slow.
        mTagIncludeFunctionName = includeFunctionName && CLog.DEBUG_MODE;
        return this;
    }

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
                result += "/" + functionCall.getLineNumber();
            }

            return result;
        } else {
            return DEFAULT_LOG_TAG;
        }
    }

    /*
    Log level can be manipulated by escalating levels based on certain criteria.
    Calculate the actual log level of the current message.
     */

    LogLevel calculateActualLogLevel(final LogLevel logLevel) {
        //Sometimes we want to bring focus to a certain logger.
        if (mImportant && CLog.DEBUG_MODE && logLevel.ordinal() < LogLevel.INFO.ordinal()) return LogLevel.INFO;

        return logLevel;
    }

    public void println(LogLevel logLevel, final String message) {
        final boolean logInReleaseMode = logLevel.ordinal() >= LogLevel.INFO.ordinal();
        println(logLevel, message, logInReleaseMode);
    }

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

    public void v(String message) {
        println(LogLevel.VERBOSE, message, false);
    }

    /*
    If for some reason you believe your verbose logging needs to be seen by clients
    use this function instead.
     */
    @SuppressWarnings("unused")
    public void v_always(final String message) {
        println(LogLevel.VERBOSE, message, true);
    }

    public void d(String message) {
        println(LogLevel.DEBUG, message, false);
    }

    public void w(String message) {
        println(LogLevel.WARN, message, true);
    }

    public void e(String message) {
        println(LogLevel.ERROR, message, true);
    }

    @SuppressWarnings("unused")
    public void wtf(String message) {
        println(LogLevel.ASSERT, message, true);
    }

    public void setLogLevel(LogLevel logLevel) {
        mLogLevel = logLevel;
    }
}


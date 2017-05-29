/*
 *    Copyright 2017 Chris McMeeking
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.chriscm.clog;

import android.os.Build;
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

    static String DEFAULT_LOG_TAG = "ChrisLogger";
    static boolean DEBUG_MODE = false;
    static boolean isInitialized = false;
    static List<String> TEST_FINGERPRINTS = Arrays.asList("robolectric");

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
        if (isInitialized) {
            CLog.w("Did you really intend to re-initialize?");
        }

        DEBUG_MODE = debugMode;
        DEFAULT_LOG_TAG = defaultTag;
        isInitialized = true;
    }

    /**
     * Prints the given message to the given log level.  The input level may not be the
     * same as the output level.  Messages below the info level can be escalated up to
     * the info log level.
     * @param message The message to be logged.
     * @param logLevel The target log level.
     */
    public static void println(final String message, final LogLevel logLevel) {

        if (!DEBUG_MODE && logLevel.ordinal() < LogLevel.INFO.ordinal()) return;

        //If it's a testing environment print to STDOUT
        if (TEST_FINGERPRINTS.contains(Build.FINGERPRINT)) {
            System.out.println(logLevel.toString() + " - " + getLogTag() + ": "  + message);
        }

        Log.println(logLevel.mAssociatedAndroidLevel, getLogTag(), message);
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

    private static String getLogTag() {

        if (!CLog.DEBUG_MODE) return DEFAULT_LOG_TAG;

        final StackTraceElement functionCall;

        try {
            functionCall = getFirstStackTraceElementNotInPackage();
        } catch (StackTraceElementNotFound stackTraceElementNotFound) {
            return DEFAULT_LOG_TAG;
        }

        return functionCall.getFileName() + ":" + functionCall.getLineNumber() + "/";
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
    static StackTraceElement getFirstStackTraceElementNotInPackage() throws StackTraceElementNotFound {

        boolean elementInClassFound = false;

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {

            //We want to search through all elements, until we find one associated with this class
            //Then grab the first one not associated with this class.
            if (element.getClassName().contains(CLog.class.getSimpleName())) {
                elementInClassFound = true;
            } else {
                if (elementInClassFound) {
                    return element;
                }
            }
        }

        throw new StackTraceElementNotFound("Went through entire loop without finding calling element.");
    }

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
}

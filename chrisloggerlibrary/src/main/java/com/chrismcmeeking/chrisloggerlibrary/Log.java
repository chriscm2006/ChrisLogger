package com.chrismcmeeking.chrisloggerlibrary;

import java.util.HashMap;

import static com.chrismcmeeking.chrisloggerlibrary.Utils.*;

/**
 * Created by chrismcmeeking on 3/8/16.
 */
public class Log {

    static HashMap<Class <? extends Object>, Logger> mLoggers = new HashMap<>();

    static Logger mDefaultLogger = new Logger();

    public static void v(final String message) {
        println(message, Logger.LogLevel.VERBOSE);
    }

    public static void w(final String message) {
        println(message, Logger.LogLevel.WARN);
    }

    public static void e(final String message) {
        println(message, Logger.LogLevel.ERROR);
    }

    public static void d(final String message) {
        println(message, Logger.LogLevel.DEBUG);
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
}

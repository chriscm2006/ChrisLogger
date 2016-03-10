package com.chriscm.clog;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chrismcmeeking on 3/8/16.
 *
 * Utility functions.
 */
public class Utils {

    private static List<Class<?>> CLASSES_IN_PACKAGE = Arrays.asList(CLog.class, Logger.class, Utils.class);

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

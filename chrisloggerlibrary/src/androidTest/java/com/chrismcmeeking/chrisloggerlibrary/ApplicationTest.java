package com.chrismcmeeking.chrisloggerlibrary;

import org.junit.Test;
/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends AndroidTestCase {

    @Test
    public void testDefaultConstruction() {
        Logger logger = new Logger();

        assertThat(logger.d(""), is(true));
        assertThat(logger.d(""), is(false));
    }
}
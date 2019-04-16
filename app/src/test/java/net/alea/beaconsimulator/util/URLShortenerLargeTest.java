package net.alea.beaconsimulator.util;


import android.support.test.filters.LargeTest;

import net.alea.beaconsimulator.BuildConfig;
import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.util.URLShortener;

import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.URL;


import static org.junit.Assert.*;


@LargeTest
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = "/AndroidManifest.xml")
public class URLShortenerLargeTest {

    private final static String URL_GOOD = "http://www.alea.net/";
    private final static String BAD_KEY = "dummy_key";

    private String mApiKey;

    @Before
    public void setUp() {
        mApiKey = RuntimeEnvironment.application.getResources().getString(R.string.bitly_generic_token);
    }

    @Ignore
    @Test(expected=IOException.class)
    public void badApiKey() throws IOException {
        new URLShortener(BAD_KEY).minifyUrl(new URL(URL_GOOD));
    }

    @Test
    public void goodUrl() throws IOException {
        URL result = new URLShortener(mApiKey).minifyUrl(new URL(URL_GOOD));
        assertThat(result, notNullValue());
    }

}

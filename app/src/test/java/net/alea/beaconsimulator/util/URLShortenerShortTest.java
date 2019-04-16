package net.alea.beaconsimulator.util;

import android.support.test.filters.MediumTest;

import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@MediumTest
public class URLShortenerShortTest {

    private final static String DUMMY_KEY = "dummy_key";

    private final static URL URL_BITLY;
    private final static URL URL_GOOD;
    static {
        try {
            URL_BITLY = new URL("http://bit.ly/2HaWukw");
            URL_GOOD =  new URL("http://www.alea.net/");
        } catch (MalformedURLException e) {
           throw new RuntimeException(e);
        }
    }


    private static class UrlShortenerTestSpecific extends URLShortener {
        public UrlShortenerTestSpecific(String apiKey) {
            super(apiKey);
        }
        @Override
        URL callExternalMinifierService(URL urlToConvert) throws IOException {
            // Disable HTTP call
            return null;
        }
    }


    @Test(expected=IOException.class)
    public void nullUrl() throws IOException {
        new UrlShortenerTestSpecific(DUMMY_KEY).minifyUrl(null);
    }


    @Test
    public void alreadyBitlyUrl() throws IOException {
        // Fixture
        URLShortener urlShortener = spy(new UrlShortenerTestSpecific(DUMMY_KEY));
        doReturn(URL_BITLY).when(urlShortener).callExternalMinifierService((URL)any());
        // Test
        URL resultUrl = urlShortener.minifyUrl(URL_BITLY);
        // Verify
        assertThat(resultUrl, equalTo(URL_BITLY));
        verify(urlShortener, never()).callExternalMinifierService((URL) any());
    }


}

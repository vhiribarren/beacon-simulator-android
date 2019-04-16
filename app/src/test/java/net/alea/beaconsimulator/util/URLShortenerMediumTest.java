package net.alea.beaconsimulator.util;

import android.support.test.filters.MediumTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@MediumTest
public class URLShortenerMediumTest {

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

    private MockWebServer mServer;
    private URL mServiceUrl;


    @Before
    public void setUp() throws IOException{
        mServer = new MockWebServer();
        mServer.start();
        mServiceUrl = mServer.url("/").url();
    }

    @After
    public void tearDown() throws IOException {
        mServer.shutdown();
    }


    @Test
    public void alreadyGooGlUrl() throws IOException {
        URL resultUrl = new URLShortener(mServiceUrl).minifyUrl(URL_BITLY);
        assertThat(resultUrl, equalTo(URL_BITLY));
        assertThat(mServer.getRequestCount(), equalTo(0));
    }


    @Test(expected=IOException.class)
    public void httpErrorResponseCode() throws IOException {
        enqueueServerError();
        new URLShortener(mServiceUrl).minifyUrl(URL_GOOD);
    }


    @Test
    public void goodRequest() throws IOException {
        enqueueGoodResponse();
        new URLShortener(mServiceUrl).minifyUrl(URL_GOOD);
        assertThat(mServer.getRequestCount(), equalTo(1));
    }


    @Test
    public void invalidHttpBodyAnswer() throws MalformedURLException {
        enqeueBadResponseContent();
        //noinspection EmptyCatchBlock
        try {
            new URLShortener(mServiceUrl).minifyUrl(URL_GOOD);
            fail();
        }
        catch(IOException e) { }
        assertThat(mServer.getRequestCount(), equalTo(1));
    }


    @Test
    public void answerNotUrl() throws MalformedURLException {
        enqueueBadResponseUrl();
        //noinspection EmptyCatchBlock
        try {
            new URLShortener(mServiceUrl).minifyUrl(URL_GOOD);
            fail();
        }
        catch(IOException e) { }
        assertThat(mServer.getRequestCount(), equalTo(1));
    }


    private void enqueueServerError() {
        mServer.enqueue(new MockResponse().setResponseCode(500));
    }


    private void enqueueGoodResponse() {
        mServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\n" +
                                "    \"created_at\": \"1970-01-01T00:00:00+0000\",\n" +
                                "    \"id\": \"bit.ly/2HaWukw\",\n" +
                                "    \"link\": \"http://bit.ly/2HaWukw\",\n" +
                                "    \"custom_bitlinks\": [],\n" +
                                "    \"long_url\": \"http://www.alea.net/\",\n" +
                                "    \"archived\": false,\n" +
                                "    \"tags\": [],\n" +
                                "    \"deeplinks\": [],\n" +
                                "    \"references\": {\n" +
                                "        \"group\": \"https://api-ssl.bitly.com/v4/groups/Bi7t9AD2xjj\"\n" +
                                "    }\n" +
                                "}")
        );
    }


    private void enqeueBadResponseContent() {
        mServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("hello world")
        );
    }


    private void enqueueBadResponseUrl() {
        mServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"longUrl\": \"http://www.google.com/\",\"id\": \"coucou\", \"kind\": \"urlshortener#url\"}")
        );
    }

}

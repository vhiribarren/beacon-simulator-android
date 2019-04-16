/****************************************************************************************
 * Copyright (c) 2016, 2017, 2019 Vincent Hiribarren                                    *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * Linking Beacon Simulator statically or dynamically with other modules is making      *
 * a combined work based on Beacon Simulator. Thus, the terms and conditions of         *
 * the GNU General Public License cover the whole combination.                          *
 *                                                                                      *
 * As a special exception, the copyright holders of Beacon Simulator give you           *
 * permission to combine Beacon Simulator program with free software programs           *
 * or libraries that are released under the GNU LGPL and with independent               *
 * modules that communicate with Beacon Simulator solely through the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces. You may           *
 * copy and distribute such a system following the terms of the GNU GPL for             *
 * Beacon Simulator and the licenses of the other code concerned, provided that         *
 * you include the source code of that other code when and as the GNU GPL               *
 * requires distribution of source code and provided that you do not modify the         *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces.                   *
 *                                                                                      *
 * The intent of this license exception and interface is to allow Bluetooth low energy  *
 * closed or proprietary advertise data packet structures and contents to be sensibly   *
 * kept closed, while ensuring the GPL is applied. This is done by using an interface   *
 * which only purpose is to generate android.bluetooth.le.AdvertiseData objects.        *
 *                                                                                      *
 * This exception is an additional permission under section 7 of the GNU General        *
 * Public License, version 3 (“GPLv3”).                                                 *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package net.alea.beaconsimulator.util;


import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class URLShortener {

    private final static Logger sLogger = LoggerFactory.getLogger(URLShortener.class);
    private final static String BITLY_SHORTEN = "https://api-ssl.bitly.com/v4/shorten";
    private final static String BITLY_GROUP_GUID = "Bi7t9AD2xjj";

    private final URL mServiceUrl;
    private final String mBearerToken;


    public URLShortener(String apiKey) {
        try {
            mServiceUrl = new URL(BITLY_SHORTEN);
            mBearerToken = apiKey;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


     // Package-level to inject a different endpoint to ease tests
     URLShortener(URL mockUrl) {
        mServiceUrl = mockUrl;
        mBearerToken = null;
    }


    public URL minifyUrl(URL url) throws IOException {
        sLogger.debug("Trying to minify URL: {}", url);
        if (url == null) {
            sLogger.debug("URL is null, not converting");
            throw new IOException("Cannot convert this URL."); // TODO Should launch an InvalidUrlException
        }
        if (isAlreadyConverted(url)) {
            sLogger.debug("URL starts with goo.gl, not converting");
            return url;
        }
        return callExternalMinifierService(url);
    }


    private boolean isAlreadyConverted(URL url) {
        return url.getHost().startsWith("bit.ly");
    }


    // Package-level to inject a different endpoint to ease tests
    URL callExternalMinifierService(URL urlToConvert) throws IOException {
        final HttpURLConnection urlConnection = buildHttpUrlConnection(urlToConvert);
        if (hasHttpError(urlConnection)) {
            final String errorString = convertResponseStreamToString(urlConnection.getErrorStream());
            final String errorMessage = parseErrorResult(errorString);
            throw new IOException(errorMessage);
        }
        else {
            String bodyMessage = convertResponseStreamToString(urlConnection.getInputStream());
            return parseMinifierResult(bodyMessage);
        }
    }


    private HttpURLConnection buildHttpUrlConnection(URL urlToConvert) throws IOException {
        JSONObject bodyRequest;
        try {
            bodyRequest = new JSONObject();
            bodyRequest.put("long_url", urlToConvert.toString());
            bodyRequest.put("group_guid", BITLY_GROUP_GUID);
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
        HttpURLConnection urlConnection = (HttpURLConnection)mServiceUrl.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setRequestProperty("Authorization", "Bearer "+mBearerToken);
        urlConnection.setDoOutput(true);
        urlConnection.getOutputStream().write(bodyRequest.toString().getBytes());
        return urlConnection;
    }


    private boolean hasHttpError(HttpURLConnection urlConnection) throws IOException {
        return urlConnection.getResponseCode() >= 400;
    }


    private URL parseMinifierResult(String result) throws IOException {
        try {
            JSONObject object = new JSONObject(result);
            String resultString = object.getString("link");
            sLogger.debug("Minifier result: {}", resultString);
            return new URL(resultString);
        } catch (JSONException |MalformedURLException e) {
            throw new IOException(e);
        }
    }

    private String parseErrorResult(String errorResult) throws IOException {
        try {
            JSONObject object = new JSONObject(errorResult);
            String resultString = object.has("description")
                    ? object.getString("description")
                    : object.getString("message");

            sLogger.warn("Error result: {}", resultString);
            return resultString;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private String convertResponseStreamToString(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

}

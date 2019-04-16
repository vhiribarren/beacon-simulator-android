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

package net.alea.beaconsimulator.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.EddystoneURL;
import net.alea.beaconsimulator.util.URLShortener;

import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;


public class ViewEditEddystoneUrl extends FrameLayout implements BeaconModelEditor {

    private static final Logger sLogger = LoggerFactory.getLogger(ViewEditEddystoneUrl.class);

    private final static int MAX_COMPRESSED_SIZE = 18;

    private final TextInputLayout mUrlLayout;
    private final TextInputLayout mPowerLayout;
    private final TextInputEditText mUrlValue;
    private final TextInputEditText mPowerValue;
    private final TextView mTxPowerInfo;
    private final Button mShortenerButton;
    private ShortenerTask mShortenerTask;

    public ViewEditEddystoneUrl(final Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_eddystone_url_edit, this);

        mTxPowerInfo = (TextView)view.findViewById(R.id.cardeddystoneurl_textview_powerinfo);
        mUrlLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneurl_textinputlayout_url);
        mPowerLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneurl_textinputlayout_power);
        mUrlValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneurl_textinput_url);
        mUrlValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkUrlValue();
            }
        });

        mPowerValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneurl_textinput_power);
        mPowerValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPowerValue();
            }
        });

        mShortenerButton = (Button)view.findViewById(R.id.cardeddystoneurl_button_shortener);
        mShortenerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    URL targetUrl = new URL(mUrlValue.getText().toString());
                    if (!targetUrl.getProtocol().startsWith("http")) {
                        throw new MalformedURLException();
                    }
                    if (mShortenerTask != null) {
                        sLogger.debug("Cancelling previous shortener task");
                        mShortenerTask.cancel(true);
                    }
                    // Using getIdentifier() to allow compilation even if bitly_generic_token is not defined
                    final int res = context
                            .getResources()
                            .getIdentifier("bitly_generic_token", "string", context.getPackageName());
                    if (res == 0) {
                        throw new MissingResourceException("The Bitly API key is missing", "R.string", "bitly_generic_token");
                    }
                    mShortenerTask = new ShortenerTask(getResources().getString(res));
                    mShortenerTask.execute(targetUrl);
                } catch (MalformedURLException e) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.shortener_format_title)
                            .setMessage(R.string.shortener_format_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) { }
                            })
                            .show();
                } catch (MissingResourceException e) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(e.getClass().getCanonicalName())
                            .setMessage(e.getMessage())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) { }
                            })
                            .show();
                }

            }
        });
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        EddystoneURL eddUrl = model.getEddystoneURL();
        if (eddUrl == null) {
            return;
        }
        mUrlValue.setText(eddUrl.getUrl());
        mPowerValue.setText(String.format(Locale.ENGLISH, "%d", eddUrl.getPower()));
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        if ( ! checkAll() ) {
            return false;
        }
        EddystoneURL eddUrl = new EddystoneURL();
        eddUrl.setUrl(mUrlValue.getText().toString());
        eddUrl.setPower(Integer.parseInt(mPowerValue.getText().toString()));
        model.setEddystoneURL(eddUrl);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mUrlValue.setEnabled(editMode);
        mPowerValue.setEnabled(editMode);
        mTxPowerInfo.setVisibility(editMode ? View.VISIBLE : View.GONE);
        mShortenerButton.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private boolean checkUrlValue() {
        final String url = mUrlValue.getText().toString();
        if (url.length() == 0) {
            mUrlLayout.setError(null);
            return true;
        }
        try {
            byte[] result = UrlBeaconUrlCompressor.compress(url);
            if (result.length > MAX_COMPRESSED_SIZE) {
                throw new MalformedURLException("URL too long");
            }
            mUrlLayout.setError(null);
            return true;
        } catch (MalformedURLException e) {
            mUrlLayout.setError(getResources().getString(R.string.edit_error_eddystone_url));
            return false;
        }
    }

    private boolean checkPowerValue() {
        boolean isValid = false;
        try {
            int power = Integer.parseInt(mPowerValue.getText().toString());
            if (power >= -128 && power <= 127) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mPowerLayout.setError(null);
        }
        else {
            mPowerLayout.setError(getResources().getString(R.string.edit_error_signed_byte));
        }
        return isValid;
    }

    private boolean checkAll() {
        return checkPowerValue() & checkUrlValue();
    }

    private abstract class SimplifiedTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    private class ShortenerTask extends AsyncTask<URL, Void, URL> {

        private Exception _exception;
        private final String _apiKey;

        public ShortenerTask(String apiKey) {
            _apiKey = apiKey;
        }

        @Override
        protected URL doInBackground(URL... params) {
            try {
                return new URLShortener(_apiKey).minifyUrl(params[0]);
            } catch (IOException e) {
                _exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(URL url) {
            mShortenerTask = null;
            if (_exception != null || url == null) {
                final String message =
                        getContext().getString(R.string.shortener_network_message, _exception.getMessage());
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.shortener_network_title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) { }
                        })
                        .show();
                return;
            }
            mUrlValue.setText(url.toString());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sLogger.debug("onDetachedFromWindow()");
        if (mShortenerTask != null) {
            sLogger.debug("Cancelling previous shortener task");
            mShortenerTask.cancel(true);
            mShortenerTask = null;
        }

    }
}

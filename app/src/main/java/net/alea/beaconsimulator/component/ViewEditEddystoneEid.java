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

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.ByteTools;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.EddystoneEID;

import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.Locale;


public class ViewEditEddystoneEid extends FrameLayout implements BeaconModelEditor {

    private final static int SIZE_HEXA_IDENTITY_KEY = 32;

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateViewRunnable = new Runnable() {
        @Override
        public void run() {
            updateView();
            mHandler.postDelayed(mUpdateViewRunnable, 1000);
        }
    };

    private final TextInputLayout mIdentityKeyLayout;
    private final TextInputEditText mIdentityKeyValue;
    private final Spinner mRotationExponentSpinner;
    private final TextInputLayout mCounterOffsetLayout;
    private final TextInputEditText mCounterOffsetValue;
    private final TextView mRealtimeCounterText;
    private final TextView mCurrentEidValue;
    private final TextView mCurrentCountdownValue;
    private final Button mIdentityKeyGenerationButton;
    private final TextInputLayout mPowerLayout;
    private final TextInputEditText mPowerValue;
    private final TextView mTxPowerInfo;


    public ViewEditEddystoneEid(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_eddystone_eid_edit, this);

        mIdentityKeyLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneeid_textinputlayout_idkey);
        mIdentityKeyValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneeid_textinput_idkey);
        mIdentityKeyValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > SIZE_HEXA_IDENTITY_KEY) {
                    s.delete(SIZE_HEXA_IDENTITY_KEY, s.length());
                }
                checkIdentityKey();
                updateView();
            }
        });
        mRotationExponentSpinner = (Spinner)view.findViewById(R.id.cardeddystoneeid_spinner_rotation);
        mRotationExponentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mCounterOffsetLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneeid_textinputlayout_offset);
        mCounterOffsetValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneeid_textinput_offset);
        mCounterOffsetValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkCounterOffset();
                updateView();
            }
        });
        mRealtimeCounterText = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_currentcounter);
        mCurrentEidValue = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_currenteidvalue);
        mCurrentCountdownValue = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_currentcountdown);
        mIdentityKeyGenerationButton = (Button)view.findViewById(R.id.cardeddystoneeid_button_keygenerator);
        mIdentityKeyGenerationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIdentityKeyValue.setText(EddystoneEID.generateRandomIdentityKey());
            }
        });
        mPowerLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneeid_textinputlayout_power);
        mPowerValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneeid_textinput_power);
        mPowerValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPowerValue();
            }
        });
        mTxPowerInfo = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_powerinfo);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.postDelayed(mUpdateViewRunnable, 1000);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mUpdateViewRunnable);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            mHandler.postDelayed(mUpdateViewRunnable, 1000);
        }
        else {
            mHandler.removeCallbacks(mUpdateViewRunnable);
        }
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        EddystoneEID eddEid = model.getEddystoneEID();
        if (eddEid == null) {
            return;
        }
        mIdentityKeyValue.setText(eddEid.getIdentityKey());
        mRotationExponentSpinner.setSelection(eddEid.getRotationPeriodExponent());
        mCounterOffsetValue.setText(String.format(Locale.ENGLISH, "%d", eddEid.getCounterOffset()));
        mPowerValue.setText(String.format(Locale.ENGLISH, "%d", eddEid.getPower()));
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        if ( ! checkAll() ) {
            return false;
        }
        EddystoneEID eddEid = new EddystoneEID();
        eddEid.setIdentityKey(mIdentityKeyValue.getText().toString());
        eddEid.setRotationPeriodExponent((byte)mRotationExponentSpinner.getSelectedItemPosition());
        eddEid.setCounterOffset(Integer.parseInt(mCounterOffsetValue.getText().toString()));
        eddEid.setPower(Integer.parseInt(mPowerValue.getText().toString()));
        model.setEddystoneEID(eddEid);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mIdentityKeyValue.setEnabled(editMode);
        mRotationExponentSpinner.setEnabled(editMode);
        mCounterOffsetValue.setEnabled(editMode);
        mPowerValue.setEnabled(editMode);
        mIdentityKeyGenerationButton.setVisibility(editMode ? View.VISIBLE : View.GONE);
        mTxPowerInfo.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private void updateView() {
        if (!checkAll()) {
            mCurrentEidValue.setText("");
            mRealtimeCounterText.setText("");
            mCurrentCountdownValue.setText("");
            return;
        }
        EddystoneEID tmpModel = new EddystoneEID();
        tmpModel.setIdentityKey(mIdentityKeyValue.getText().toString());
        tmpModel.setRotationPeriodExponent((byte)mRotationExponentSpinner.getSelectedItemPosition());
        tmpModel.setCounterOffset(Integer.parseInt(mCounterOffsetValue.getText().toString()));
        tmpModel.setPower(Integer.parseInt(mPowerValue.getText().toString()));
        try {
            mCurrentEidValue.setText(ByteTools.bytesToHex(tmpModel.generateEidIdentifier()));
            mRealtimeCounterText.setText(NumberFormat.getInstance().format(tmpModel.getBeaconCounter()));
            mCurrentCountdownValue.setText(NumberFormat.getInstance().format(tmpModel.getBeaconCountdown()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkCounterOffset() {
        boolean isValid = false;
        try {
            long offset = Long.parseLong(mCounterOffsetValue.getText().toString());
            if (offset >= Integer.MIN_VALUE && offset <= Integer.MAX_VALUE) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mCounterOffsetLayout.setError(null);
        }
        else {
            mCounterOffsetLayout.setError(getResources().getString(R.string.edit_error_signed_int));
        }
        return isValid;
    }

    private boolean checkIdentityKey() {
        final String namespace = mIdentityKeyValue.getText().toString();
        if (namespace.length() == SIZE_HEXA_IDENTITY_KEY) {
            mIdentityKeyLayout.setError(null);
            return true;
        }
        else {
            mIdentityKeyLayout.setError(getResources().getString(R.string.edit_error_identity_key_size));
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkAll() {
        return checkCounterOffset() & checkIdentityKey() & checkPowerValue();
    }

    private abstract class SimplifiedTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }



}

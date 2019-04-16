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
import net.alea.beaconsimulator.bluetooth.model.AltBeacon;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;

import java.util.Locale;
import java.util.UUID;


public class ViewEditAltBeacon extends FrameLayout implements BeaconModelEditor {

    private final static int SIZE_HEXA_RESERVED_VALUE = 2;

    private final TextInputLayout mUuidLayout;
    private final TextInputLayout mMajorLayout;
    private final TextInputLayout mMinorLayout;
    private final TextInputLayout mPowerLayout;
    private final TextInputLayout mManufacturerIdLayout;
    private final TextInputLayout mManufacturerReservedLayout;
    private final TextInputEditText mUuidValue;
    private final TextInputEditText mMajorValue;
    private final TextInputEditText mMinorValue;
    private final TextInputEditText mPowerValue;
    private final TextInputEditText mManufacturerIdValue;
    private final TextInputEditText mManufacturerReservedValue;
    private final TextView mTxPowerInfo;
    private final Button mUuidButton;

    public ViewEditAltBeacon(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_altbeacon_edit, this);

        mTxPowerInfo = (TextView)view.findViewById(R.id.cardaltbeacon_textview_power);
        mUuidLayout = (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_uuid);
        mMajorLayout = (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_major);
        mMinorLayout = (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_minor);
        mPowerLayout = (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_power);
        mManufacturerIdLayout =  (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_manufacturerid);
        mManufacturerReservedLayout =  (TextInputLayout)view.findViewById(R.id.cardaltbeacon_textinputlayout_reserved);
        mUuidValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_uuid);
        mUuidValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkUuidValue();
            }
        });
        mMajorValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_major);
        mMajorValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkMajorValue();
            }
        });
        mMinorValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_minor);
        mMinorValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkMinorValue();
            }
        });
        mPowerValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_power);
        mPowerValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPowerValue();
            }
        });
        mManufacturerIdValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_manufacturerid);
        mManufacturerIdValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkManufacturerIdValue();
            }
        });
        mManufacturerReservedValue = (TextInputEditText)view.findViewById(R.id.cardaltbeacon_textinput_reserved);
        mManufacturerReservedValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > SIZE_HEXA_RESERVED_VALUE) {
                    s.delete(SIZE_HEXA_RESERVED_VALUE, s.length());
                }
                checkManufacturerReservedValue();
            }
        });
        mUuidButton = (Button)view.findViewById(R.id.cardaltbeacon_button_generateuuid);
        mUuidButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUuidValue.setText(UUID.randomUUID().toString());
            }
        });
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        AltBeacon altBeacon = model.getAltbeacon();
        if (altBeacon == null) {
            return;
        }
        mUuidValue.setText(altBeacon.getBeaconNamespace().toString());
        mMajorValue.setText(String.format(Locale.ENGLISH, "%d", altBeacon.getMajor()));
        mMinorValue.setText(String.format(Locale.ENGLISH, "%d", altBeacon.getMinor()));
        mPowerValue.setText(String.format(Locale.ENGLISH, "%d", altBeacon.getPower()));
        mManufacturerIdValue.setText(String.format(Locale.ENGLISH, "%d", altBeacon.getManufacturerId()));
        mManufacturerReservedValue.setText(altBeacon.getManufacturerReserved());
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        if ( ! checkAll() ) {
            return false;
        }
        AltBeacon altBeacon = new AltBeacon();
        altBeacon.setBeaconNamespace(UUID.fromString(mUuidValue.getText().toString()));
        altBeacon.setMajor(Integer.parseInt(mMajorValue.getText().toString()));
        altBeacon.setMinor(Integer.parseInt(mMinorValue.getText().toString()));
        altBeacon.setPower(Byte.parseByte(mPowerValue.getText().toString()));
        altBeacon.setManufacturerId(Integer.parseInt(mManufacturerIdValue.getText().toString()));
        altBeacon.setManufacturerReserved(mManufacturerReservedValue.getText().toString());
        model.setAltBeacon(altBeacon);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mUuidValue.setEnabled(editMode);
        mMajorValue.setEnabled(editMode);
        mMinorValue.setEnabled(editMode);
        mPowerValue.setEnabled(editMode);
        mManufacturerIdValue.setEnabled(editMode);
        mManufacturerReservedValue.setEnabled(editMode);
        mUuidButton.setVisibility(editMode ? View.VISIBLE : View.GONE);
        mTxPowerInfo.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private boolean checkUuidValue() {
        try {
            final String uuid = mUuidValue.getText().toString();
            if (uuid.length() < 36) {
                throw new IllegalArgumentException();
            }
            //noinspection ResultOfMethodCallIgnored
            UUID.fromString(uuid);
            mUuidLayout.setError(null);
        }
        catch (IllegalArgumentException e) {
            mUuidLayout.setError(getResources().getString(R.string.edit_error_uuid));
            return false;
        }
        return true;
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

    private boolean checkMajorValue() {
        boolean isValid = false;
        try {
            int major = Integer.parseInt(mMajorValue.getText().toString());
            if (major >= 0 && major <= 65535) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mMajorLayout.setError(null);
        }
        else {
            mMajorLayout.setError(getResources().getString(R.string.edit_error_unsigned_short));
        }
        return isValid;
    }

    private boolean checkMinorValue() {
        boolean isValid = false;
        try {
            int minor = Integer.parseInt(mMinorValue.getText().toString());
            if (minor >= 0 && minor <= 65535) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mMinorLayout.setError(null);
        }
        else {
            mMinorLayout.setError(getResources().getString(R.string.edit_error_unsigned_short));
        }
        return isValid;
    }

    private boolean checkManufacturerIdValue() {
        boolean isValid = false;
        try {
            int manufacturerId = Integer.parseInt(mManufacturerIdValue.getText().toString());
            if (manufacturerId >= 0 && manufacturerId <= 65535) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mManufacturerIdLayout.setError(null);
        }
        else {
            mManufacturerIdLayout.setError(getResources().getString(R.string.edit_error_unsigned_short));
        }
        return isValid;
    }

    private boolean checkManufacturerReservedValue() {
        final String reservedValue = mManufacturerReservedValue.getText().toString();
        if (reservedValue.length() == 2) {
            mManufacturerReservedLayout.setError(null);
            return true;
        }
        else {
            mManufacturerReservedLayout.setError(getResources().getString(R.string.edit_error_manufacturer_reserved));
            return false;
        }
    }

    private boolean checkAll() {
        return checkPowerValue() & checkUuidValue() & checkMajorValue() & checkMinorValue()
                && checkManufacturerIdValue() && checkManufacturerReservedValue();
    }

    private abstract class SimplifiedTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }



}

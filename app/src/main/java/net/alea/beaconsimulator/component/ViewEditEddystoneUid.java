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
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.EddystoneUID;

import java.util.Locale;


public class ViewEditEddystoneUid extends FrameLayout implements BeaconModelEditor {

    private final static int SIZE_HEXA_NAMESPACE = 2*10;
    private final static int SIZE_HEX_INSTANCE = 2*6;

    private final TextInputLayout mUidNamespaceLayout;
    private final TextInputLayout mUidInstanceLayout;
    private final TextInputLayout mPowerLayout;
    private final TextInputEditText mUidNamespaceValue;
    private final TextInputEditText mUidInstanceValue;
    private final TextInputEditText mPowerValue;
    private final TextView mTxPowerInfo;
    private final Button mUidNamespaceButton;

    public ViewEditEddystoneUid(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_eddystone_uid_edit, this);

        mTxPowerInfo = (TextView)view.findViewById(R.id.cardeddystoneuid_textview_powerinfo);
        mUidNamespaceLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneuid_textinputlayout_namespace);
        mUidInstanceLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneuid_textinputlayout_instance);
        mPowerLayout = (TextInputLayout)view.findViewById(R.id.cardeddystoneuid_textinputlayout_power);
        mUidNamespaceValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneuid_textinput_namespace);
        mUidNamespaceValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > SIZE_HEXA_NAMESPACE) {
                    s.delete(SIZE_HEXA_NAMESPACE, s.length());
                }
                checkUidNamespaceValue();
            }
        });
        mUidInstanceValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneuid_textinput_instance);
        mUidInstanceValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > SIZE_HEX_INSTANCE) {
                    s.delete(SIZE_HEX_INSTANCE, s.length());
                }
                checkUidInstanceValue();
            }
        });
        mPowerValue = (TextInputEditText)view.findViewById(R.id.cardeddystoneuid_textinput_power);
        mPowerValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkPowerValue();
            }
        });
        mUidNamespaceButton = (Button)view.findViewById(R.id.cardeddystoneuid_button_generatenamespace);
        mUidNamespaceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUidNamespaceValue.setText(EddystoneUID.generateUidNamespace());
            }
        });
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        EddystoneUID eddUid = model.getEddystoneUID();
        if (eddUid == null) {
            return;
        }
        mUidNamespaceValue.setText(eddUid.getNamespace());
        mUidInstanceValue.setText(eddUid.getInstance());
        mPowerValue.setText(String.format(Locale.ENGLISH, "%d", eddUid.getPower()));
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        if ( ! checkAll() ) {
            return false;
        }
        EddystoneUID eddUid = new EddystoneUID();
        eddUid.setNamespace(mUidNamespaceValue.getText().toString());
        eddUid.setInstance(mUidInstanceValue.getText().toString());
        eddUid.setPower(Integer.parseInt(mPowerValue.getText().toString()));
        model.setEddystoneUID(eddUid);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mUidNamespaceValue.setEnabled(editMode);
        mUidInstanceValue.setEnabled(editMode);
        mPowerValue.setEnabled(editMode);
        mUidNamespaceButton.setVisibility(editMode ? View.VISIBLE : View.GONE);
        mTxPowerInfo.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private boolean checkUidNamespaceValue() {
        final String namespace = mUidNamespaceValue.getText().toString();
        if (namespace.length() == SIZE_HEXA_NAMESPACE) {
            mUidNamespaceLayout.setError(null);
            return true;
        }
        else {
            mUidNamespaceLayout.setError(getResources().getString(R.string.edit_error_uid_namespace));
            return false;
        }
    }

    private boolean checkUidInstanceValue() {
        final String instance = mUidInstanceValue.getText().toString();
        if (instance.length() == SIZE_HEX_INSTANCE) {
            mUidInstanceLayout.setError(null);
            return true;
        }
        else {
            mUidInstanceLayout.setError(getResources().getString(R.string.edit_error_uid_instance));
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
        return checkPowerValue() & checkUidNamespaceValue() & checkUidInstanceValue();
    }

    private abstract class SimplifiedTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }



}

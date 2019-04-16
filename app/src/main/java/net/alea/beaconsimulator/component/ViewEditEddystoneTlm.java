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
import android.widget.FrameLayout;

import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.EddystoneTLM;

import java.util.Locale;


public class ViewEditEddystoneTlm extends FrameLayout implements BeaconModelEditor {

    private final TextInputLayout mVoltageLayout;
    private final TextInputLayout mTemperatureLayout;
    private final TextInputLayout mAdvertisingCountLayout;
    private final TextInputLayout mUptimeLayout;
    private final TextInputEditText mVoltageValue;
    private final TextInputEditText mTemperatureValue;
    private final TextInputEditText mAdvertisingCountValue;
    private final TextInputEditText mUptimeValue;


    public ViewEditEddystoneTlm(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_eddystone_tlm_edit, this);

        mVoltageLayout = (TextInputLayout)view.findViewById(R.id.cardeddystonetlm_textinputlayout_voltage);
        mTemperatureLayout = (TextInputLayout)view.findViewById(R.id.cardeddystonetlm_textinputlayout_temperature);
        mAdvertisingCountLayout = (TextInputLayout)view.findViewById(R.id.cardeddystonetlm_textinputlayout_advertisingcount);
        mUptimeLayout = (TextInputLayout)view.findViewById(R.id.cardeddystonetlm_textinputlayout_uptime);
        mVoltageValue = (TextInputEditText)view.findViewById(R.id.cardeddystonetlm_textinput_voltage);
        mVoltageValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkVoltageValue();
            }
        });
        mTemperatureValue = (TextInputEditText)view.findViewById(R.id.cardeddystonetlm_textinput_temperature);
        mTemperatureValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkTemperatureValue();
            }
        });
        mAdvertisingCountValue = (TextInputEditText)view.findViewById(R.id.cardeddystonetlm_textinput_advertisingcount);
        mAdvertisingCountValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkAdvertisingCountValue();
            }
        });
        mUptimeValue = (TextInputEditText)view.findViewById(R.id.cardeddystonetlm_textinput_uptime);
        mUptimeValue.addTextChangedListener(new SimplifiedTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                checkUptimeValue();
            }
        });
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        EddystoneTLM eddTlm = model.getEddystoneTLM();
        if (eddTlm == null) {
            return;
        }
        mVoltageValue.setText(String.format(Locale.ENGLISH, "%d", eddTlm.getVoltage()));
        mTemperatureValue.setText(String.format(Locale.ENGLISH, "%f", eddTlm.getTemperature()));
        mAdvertisingCountValue.setText(String.format(Locale.ENGLISH, "%d", eddTlm.getAdvertisingCount()));
        mUptimeValue.setText(String.format(Locale.ENGLISH, "%d", eddTlm.getUptime()));
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        if ( ! checkAll() ) {
            return false;
        }
        EddystoneTLM eddTlm = new EddystoneTLM();
        eddTlm.setVoltage(Integer.parseInt(mVoltageValue.getText().toString()));
        eddTlm.setTemperature(Double.parseDouble(mTemperatureValue.getText().toString()));
        eddTlm.setAdvertisingCount(Long.parseLong(mAdvertisingCountValue.getText().toString()));
        eddTlm.setUptime(Long.parseLong(mUptimeValue.getText().toString()));
        model.setEddystoneTLM(eddTlm);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mVoltageValue.setEnabled(editMode);
        mTemperatureValue.setEnabled(editMode);
        mAdvertisingCountValue.setEnabled(editMode);
        mUptimeValue.setEnabled(editMode);
    }

    private boolean checkVoltageValue() {
        boolean isValid = false;
        try {
            int voltage = Integer.parseInt(mVoltageValue.getText().toString());
            if (voltage >= 0 && voltage < Math.pow(2, 16)) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mVoltageLayout.setError(null);
        }
        else {
            mVoltageLayout.setError(getResources().getString(R.string.edit_error_unsigned_short));
        }
        return isValid;
    }

    private boolean checkTemperatureValue() {
        boolean isValid = false;
        try {
            double temperature = Double.parseDouble(mTemperatureValue.getText().toString());
            if (temperature >= Byte.MIN_VALUE && temperature <= Byte.MAX_VALUE) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mTemperatureLayout.setError(null);
        }
        else {
            mTemperatureLayout.setError(getResources().getString(R.string.edit_error_signed_byte));
        }
        return isValid;
    }

    private boolean checkAdvertisingCountValue() {
        boolean isValid = false;
        try {
            long count = Long.parseLong(mAdvertisingCountValue.getText().toString());
            if (count >= 0 && count < Math.pow(2, 32)) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mAdvertisingCountLayout.setError(null);
        }
        else {
            mAdvertisingCountLayout.setError(getResources().getString(R.string.edit_error_unsigned_int));
        }
        return isValid;
    }

    private boolean checkUptimeValue() {
        boolean isValid = false;
        try {
            long uptime = Long.parseLong(mUptimeValue.getText().toString());
            if (uptime >= 0 && uptime < Math.pow(2, 32)) {
                isValid = true;
            }
        } catch (NumberFormatException e) {
            // not valid, isValid already false
        }
        if ( isValid ) {
            mUptimeLayout.setError(null);
        }
        else {
            mUptimeLayout.setError(getResources().getString(R.string.edit_error_unsigned_int));
        }
        return isValid;
    }

    private boolean checkAll() {
        return checkVoltageValue() & checkTemperatureValue() & checkAdvertisingCountValue() & checkUptimeValue();
    }

    private abstract class SimplifiedTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

}

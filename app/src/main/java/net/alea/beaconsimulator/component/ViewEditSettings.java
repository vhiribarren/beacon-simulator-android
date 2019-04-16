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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;

import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.model.AdvertiseMode;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.PowerLevel;
import net.alea.beaconsimulator.bluetooth.model.Settings;


public class ViewEditSettings extends FrameLayout implements BeaconModelEditor {

    private final Spinner mAdvertiseModeSpinner;
    private final Spinner mTxPowerSpinner;
    private final CheckBox mConnectable;

    public ViewEditSettings(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.card_beacon_settings_edit, this);

        mTxPowerSpinner = (Spinner)view.findViewById(R.id.cardsettings_spinner_powerlevel);
        ArrayAdapter<CharSequence> txPowerAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.power_level,
                android.R.layout.simple_spinner_item);
        txPowerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTxPowerSpinner.setAdapter(txPowerAdapter);


        mAdvertiseModeSpinner = (Spinner)view.findViewById(R.id.cardsettings_spinner_advertisemode);
        ArrayAdapter<CharSequence> advertiseModeAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.advertise_mode,
                android.R.layout.simple_spinner_item);
        advertiseModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdvertiseModeSpinner.setAdapter(advertiseModeAdapter);

        mConnectable = (CheckBox)view.findViewById(R.id.cardsettings_checkbox_connectable);
        // Disabled for now since only beacon are generated, enabling that would create confusion
        mConnectable.setVisibility(View.GONE);
    }

    private static PowerLevel fromPositionToPower(int position) {
        switch (position) {
            case 0: return PowerLevel.high;
            case 1: return PowerLevel.medium;
            case 2: return PowerLevel.low;
            case 3:
            default:
                return PowerLevel.ultraLow;
        }
    }

    private static int fromPowerToPosition(PowerLevel power) {
        switch (power) {
            case high: return 0;
            case medium: return 1;
            case low: return 2;
            case ultraLow:
            default:
                return 3;
        }
    }

    private static AdvertiseMode fromPositionToAdMode(int position) {
        switch (position) {
            case 0: return AdvertiseMode.lowLatency;
            case 1: return AdvertiseMode.balanced;
            case 2:
            default:
                return AdvertiseMode.lowPower;
        }
    }

    private static int fromAdModeToPosition(AdvertiseMode mode) {
        switch (mode) {
            case lowLatency: return 0;
            case balanced: return 1;
            case lowPower:
            default:
                return 2;
        }
    }

    @Override
    public void loadModelFrom(BeaconModel model) {
        mConnectable.setChecked(model.getSettings().isConnectable());
        mTxPowerSpinner.setSelection(fromPowerToPosition(model.getSettings().getPowerLevel()));
        mAdvertiseModeSpinner.setSelection(fromAdModeToPosition(model.getSettings().getAdvertiseMode()));
    }

    @Override
    public boolean saveModelTo(BeaconModel model) {
        Settings settings = new Settings();
        settings.setConnectable(mConnectable.isChecked());
        settings.setAdvertiseMode(fromPositionToAdMode(mAdvertiseModeSpinner.getSelectedItemPosition()));
        settings.setPowerLevel(fromPositionToPower(mTxPowerSpinner.getSelectedItemPosition()));
        model.setSettings(settings);
        return true;
    }

    @Override
    public void setEditMode(boolean editMode) {
        mTxPowerSpinner.setEnabled(editMode);
        mAdvertiseModeSpinner.setEnabled(editMode);
        mConnectable.setEnabled(editMode);
    }
}

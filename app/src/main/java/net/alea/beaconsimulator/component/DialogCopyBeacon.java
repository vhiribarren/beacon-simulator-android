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


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.alea.beaconsimulator.App;
import net.alea.beaconsimulator.R;
import net.alea.beaconsimulator.bluetooth.BeaconStore;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;

public class DialogCopyBeacon extends BottomSheetDialogFragment {


    private final static String BEACON_MODEL = "BEACON_MODEL";

    // Problem if directly instantiate fragment with parameters in constructor, and if screen rotation
    // http://stackoverflow.com/questions/9245408/best-practice-for-instantiating-a-new-android-fragment
    public static DialogCopyBeacon newInstance(BeaconModel beaconModel) {
        DialogCopyBeacon myFragment = new DialogCopyBeacon();
        Bundle args = new Bundle();
        args.putParcelable(BEACON_MODEL, beaconModel);
        myFragment.setArguments(args);
        return myFragment;
    }


    private BeaconStore _beaconStore;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _beaconStore = ((App)activity.getApplication()).getBeaconStore();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final Bundle bundle = getArguments();
        final BeaconModel beaconModel = bundle.getParcelable(BEACON_MODEL);

        final View contentView = inflater.inflate(R.layout.dialog_beacon_copy, container);
        final TextInputEditText nameValue = (TextInputEditText)contentView.findViewById(R.id.beaconcopy_textinput_name);
        nameValue.setHint(beaconModel.generateBeaconName());
        Button saveButton = (Button) contentView.findViewById(R.id.beaconcopy_button_save);
        Button cancelButton = (Button) contentView.findViewById(R.id.beaconcopy_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameValue.getEditableText().toString();
                beaconModel.setName(name.isEmpty() ? beaconModel.generateBeaconName() : name);
                _beaconStore.saveBeacon(beaconModel);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
            }
        });

        return contentView;
    }

}
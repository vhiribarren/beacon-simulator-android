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

package net.alea.beaconsimulator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.alea.beaconsimulator.bluetooth.BeaconStore;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.BeaconType;
import net.alea.beaconsimulator.component.BeaconModelEditor;
import net.alea.beaconsimulator.component.ViewEditAltBeacon;
import net.alea.beaconsimulator.component.ViewEditEddystoneEid;
import net.alea.beaconsimulator.component.ViewEditEddystoneTlm;
import net.alea.beaconsimulator.component.ViewEditEddystoneUid;
import net.alea.beaconsimulator.component.ViewEditEddystoneUrl;
import net.alea.beaconsimulator.component.ViewEditIBeacon;
import net.alea.beaconsimulator.component.ViewEditSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class FragmentBeaconEdit extends Fragment {

    private static final Logger sLogger = LoggerFactory.getLogger(FragmentBeaconEdit.class);

    private boolean mIsNewModel = false;
    private boolean mEditMode = false;
    private BeaconModel mBeaconModel;
    private BeaconModelEditor mBeaconSpecificView;
    private ViewEditSettings mSettingsView;
    private BeaconStore mBeaconStore;
    private EditText mBeaconNameField;
    private Toolbar mToolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeaconStore = ((App)getActivity().getApplication()).getBeaconStore();
        setRetainInstance(true);
        setHasOptionsMenu(true);
        final Bundle bundle = getArguments();
        // Edit existing beacon
        if (bundle.containsKey(ActivityBeaconEdit.EXTRA_ID)) {
            UUID id = (UUID)bundle.get(ActivityBeaconEdit.EXTRA_ID);
            sLogger.debug("Editing beacon {}", id);
            mBeaconModel = mBeaconStore.getBeacon(id);
            // TODO If beaconModel null error
            mEditMode = false;
            mIsNewModel = false;
        }
        // New Beacon
        else {
            BeaconType type = (BeaconType) bundle.get(ActivityBeaconEdit.EXTRA_TYPE);
            mBeaconModel = new BeaconModel(type);
            mEditMode = true;
            mIsNewModel = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_beacon_edit, container, false);

        mBeaconNameField = (EditText)view.findViewById(R.id.beaconedit_textinput_name);
        if (mIsNewModel) {
            mBeaconNameField.setHint(mBeaconModel.generateBeaconName());
        }

        mToolbar = (Toolbar) view.findViewById(R.id.beaconedit_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ActionBar actionBar =  ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.beaconedition_title));
        }

        ViewGroup cardListView = (ViewGroup)view.findViewById(R.id.beaconedit_linearlayout_cardlist);
        View beaconModelView = null;
        switch (mBeaconModel.getType()) {
            case eddystoneTLM:
                beaconModelView = new ViewEditEddystoneTlm(getContext());
                break;
            case eddystoneUID:
                beaconModelView = new ViewEditEddystoneUid(getContext());
                break;
            case eddystoneURL:
                beaconModelView = new ViewEditEddystoneUrl(getContext());
                break;
            case eddystoneEID:
                beaconModelView = new ViewEditEddystoneEid(getContext());
                break;
            case ibeacon:
                beaconModelView = new ViewEditIBeacon(getContext());
                break;
            case altbeacon:
                beaconModelView = new ViewEditAltBeacon(getContext());
                break;
            case raw:
                break;
            default:
                sLogger.warn("Unknown beacon type asked for edition");
        }
        if (beaconModelView != null) {
            cardListView.addView(beaconModelView);
            mBeaconSpecificView = (BeaconModelEditor)beaconModelView;
        }

        inflater.inflate(R.layout.view_space, cardListView);
        mSettingsView = new ViewEditSettings(getContext());
        cardListView.addView(mSettingsView);
        inflater.inflate(R.layout.view_space, cardListView);
        enableEditMode(mEditMode);
        loadBeacon();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        sLogger.debug("onPause() called");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        if (mEditMode) {
            mToolbar.inflateMenu(R.menu.fragment_beacon_edit_edition);
        }
        else {
            mToolbar.inflateMenu(R.menu.fragment_beacon_edit_readonly);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                enableEditMode(true);
                return true;
            case R.id.action_cancel: {
                if (mIsNewModel) {
                    leaveActivity();
                }
                else {
                    loadBeacon();
                    enableEditMode(false);
                }
                if (getView() != null) {
                    getView().clearFocus();
                }
                return true;
            }
            case R.id.action_save: {
                boolean success = saveBeacon();
                if (success) {
                    enableEditMode(false);
                    mIsNewModel = false;
                }
                if (getView() != null) {
                    getView().clearFocus();
                }
                return true;
            }
            case R.id.action_delete: {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.beacondelete_title)
                        .setMessage(R.string.beacondelete_message)
                        .setPositiveButton(R.string.all_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBeaconStore.deleteBeacon(mBeaconModel);
                                leaveActivity();
                            }
                        })
                        .setNegativeButton(R.string.all_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        }
        return false;
    }

    public void onBackPressed() {
        if (mEditMode) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.beaconedit_confirmation_title)
                    .setMessage(R.string.beaconedit_confirmation_message)
                    .setPositiveButton(R.string.all_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean success = saveBeacon();
                            if (success) {
                                leaveActivity();
                            }
                        }
                    })
                    .setNeutralButton(R.string.all_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton(R.string.all_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leaveActivity();
                        }
                    })
                    .show();
        }
        else {
            leaveActivity();
        }
    }

    private void leaveActivity() {
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    public void enableEditMode(boolean editMode) {
        mEditMode = editMode;
        mSettingsView.setEditMode(editMode);
        mBeaconSpecificView.setEditMode(editMode);
        mBeaconNameField.setEnabled(editMode);
        ActivityCompat.invalidateOptionsMenu(getActivity());
    }

    private boolean saveBeacon() {
        boolean isValid = mBeaconSpecificView.saveModelTo(mBeaconModel);
        isValid = isValid & mSettingsView.saveModelTo(mBeaconModel);
        if (! isValid) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Cannot save")
                    .setMessage("Invalid values, please correct or cancel edition.")
                    .setPositiveButton(R.string.all_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
            return false;
        }
        String nameValue = mBeaconNameField.getText().toString();
        if (nameValue.isEmpty() && mIsNewModel) {
            nameValue = mBeaconModel.generateBeaconName();
            mBeaconNameField.setText(nameValue);
        }
        mBeaconModel.setName(nameValue);
        mBeaconStore.saveBeacon(mBeaconModel);
        mIsNewModel = false;
        return true;
    }

    private void loadBeacon() {
        mBeaconNameField.setText(mBeaconModel.getName());
        mBeaconSpecificView.loadModelFrom(mBeaconModel);
        mSettingsView.loadModelFrom(mBeaconModel);
    }

}

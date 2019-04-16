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


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.localizationactivity.LocalizationActivity;

import net.alea.beaconsimulator.bluetooth.BeaconDiagnostic;
import net.alea.beaconsimulator.bluetooth.BeaconSimulatorService;

import java.text.NumberFormat;

public class ActivityBeaconDiagnostic extends LocalizationActivity implements BeaconDiagnostic.OnDiagnosticResult {

    public static void launchActivity(Context context) {
        Intent aboutIntent = new Intent(context, ActivityBeaconDiagnostic.class);
        context.startActivity(aboutIntent);
    }


    private View mResultContainer;
    private View  mCountContainer;
    private TextView mDiagnosticResult;
    private TextView mMaxBroadcastNb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFinishOnTouchOutside(false);
        setContentView(R.layout.activitity_beacon_diagnostic);

        mDiagnosticResult = (TextView) findViewById(R.id.diagnostic_textview_result);
        mMaxBroadcastNb = (TextView) findViewById(R.id.diagnostic_textview_maxbroadcast);
        mCountContainer = findViewById(R.id.diagnostic_linearlayout_count);
        mResultContainer = findViewById(R.id.diagnostic_linearlayout_result);
        mResultContainer.setVisibility(View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.beacondiag_toolbar);
        toolbar.setTitle(R.string.action_autodiagnostic);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button launchDiagnosticButton = (Button)findViewById(R.id.diagnostic_button_start);
        launchDiagnosticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultContainer.setVisibility(View.GONE);
                if (BeaconSimulatorService.isBluetoothOn(ActivityBeaconDiagnostic.this)) {
                    new BeaconDiagnostic().launchDiagnostic(ActivityBeaconDiagnostic.this, ActivityBeaconDiagnostic.this);
                }
                else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    public void onDiagnosticSuccess(int count) {
        mResultContainer.setVisibility(View.VISIBLE);
        mCountContainer.setVisibility(View.VISIBLE);
        mMaxBroadcastNb.setText(NumberFormat.getInstance().format((count)));
        mDiagnosticResult.setText(R.string.diagnostic_success_message);
    }

    @Override
    public void onDiagnosticFailure(int reason) {
        mResultContainer.setVisibility(View.VISIBLE);
        mCountContainer.setVisibility(View.GONE);
        int resId;
        switch (reason) {
            case BeaconDiagnostic.REASON_BLE_UNSUPPORTED:
                resId = R.string.diagnostic_error_ble_unsupported;
                break;
            case BeaconDiagnostic.REASON_BT_OFF:
                resId = R.string.diagnostic_error_bt_off;
                break;
            case BeaconDiagnostic.REASON_MAX_TRIES:
                resId = R.string.diagnostic_error_max_tries;
                break;
            case BeaconDiagnostic.REASON_UNEXPECTED_ERROR:
                resId = R.string.diagnostic_error_unexpected;
                break;
            case BeaconDiagnostic.REASON_IS_RUNNING:
            default:
                return;
        }
        mDiagnosticResult.setText(resId);
    }
}

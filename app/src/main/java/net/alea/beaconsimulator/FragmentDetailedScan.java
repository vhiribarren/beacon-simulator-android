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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.neovisionaries.bluetooth.ble.advertising.ADManufacturerSpecific;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneEID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.neovisionaries.bluetooth.ble.advertising.Flags;
import com.neovisionaries.bluetooth.ble.advertising.LocalName;
import com.neovisionaries.bluetooth.ble.advertising.ServiceData;
import com.neovisionaries.bluetooth.ble.advertising.TxPowerLevel;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;

import net.alea.beaconsimulator.bluetooth.BtNumbers;
import net.alea.beaconsimulator.bluetooth.ByteTools;
import net.alea.beaconsimulator.bluetooth.IBeaconParser;
import net.alea.beaconsimulator.bluetooth.model.AltBeacon;
import net.alea.beaconsimulator.bluetooth.model.BeaconModel;
import net.alea.beaconsimulator.bluetooth.model.BeaconType;
import net.alea.beaconsimulator.bluetooth.model.IBeacon;
import net.alea.beaconsimulator.component.DialogCopyBeacon;
import net.alea.beaconsimulator.component.ViewEditAltBeacon;
import net.alea.beaconsimulator.component.ViewEditEddystoneTlm;
import net.alea.beaconsimulator.component.ViewEditEddystoneUid;
import net.alea.beaconsimulator.component.ViewEditEddystoneUrl;
import net.alea.beaconsimulator.component.ViewEditIBeacon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class FragmentDetailedScan extends Fragment {

    private static final Logger sLogger = LoggerFactory.getLogger(FragmentDetailedScan.class);

    public static final String SCAN_RESULT = "net.alea.beaconsimulator.SCAN_RESULT";

    private BeaconModel mBeaconModel = null;
    private ScanResult mScanResult = null;
    private List<ADStructure> mAdStructures = null;
    private LayoutInflater mLayoutInflater = null;

    private BtNumbers mBtNumbers;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBtNumbers = ((App)getActivity().getApplication()).getBtNumbers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detailed_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        mLayoutInflater =  (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //ScanResult scanResult = (ScanResult)getArguments().get(SCAN_RESULT);
        mScanResult = (ScanResult)bundle.get(SCAN_RESULT);
        if (mScanResult == null) {
            return;
        }
        mAdStructures = ADPayloadParser.getInstance().parse(mScanResult.getScanRecord().getBytes());

        ViewGroup cardContainer = (ViewGroup)view.findViewById(R.id.detailedscan_linearlayout_cardlist);
        fillDeviceCard(cardContainer);
        generateCards(cardContainer); // It also fills mBeaconModel

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.detailedscan_fab_copy);
        boolean canCopy = ! (BeaconType.raw.equals(mBeaconModel.getType()) | BeaconType.eddystoneEID.equals(mBeaconModel.getType()));
        if ( canCopy ) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogCopyBeacon dialog = DialogCopyBeacon.newInstance(mBeaconModel);
                    dialog.show(getFragmentManager(), dialog.getTag());
                }
            });
        }
        else {
            fab.hide();
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.detailedscan_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar =  ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Problem of focus in some card showing some text higlighted even if set as not editable
        // http://stackoverflow.com/questions/6117967/how-to-remove-focus-without-setting-focus-to-another-control/
        view.findViewById(R.id.detailedscan_linearlayout_dummyfocusable).requestFocus();

    }


    private void generateCards(ViewGroup cardContainer) {

        for (ADStructure structure : mAdStructures) {
            if (structure instanceof EddystoneUID) {
                EddystoneUID eddystoneUidStructure = (EddystoneUID)structure;
                mBeaconModel = new BeaconModel(BeaconType.eddystoneUID);
                mBeaconModel.getEddystoneUID().setPower(eddystoneUidStructure.getTxPower());
                mBeaconModel.getEddystoneUID().setNamespace(eddystoneUidStructure.getNamespaceIdAsString());
                mBeaconModel.getEddystoneUID().setInstance(eddystoneUidStructure.getInstanceIdAsString());
                fillEddystoneUIDCard(mBeaconModel, cardContainer);
            }
            else if (structure instanceof EddystoneURL) {
                EddystoneURL eddystoneUrlStructure = (EddystoneURL)structure;
                URL url = eddystoneUrlStructure.getURL();
                mBeaconModel = new BeaconModel(BeaconType.eddystoneURL);
                mBeaconModel.getEddystoneURL().setUrl(url != null ? url.toExternalForm() : null);
                mBeaconModel.getEddystoneURL().setPower(eddystoneUrlStructure.getTxPower());
                fillEddystoneURLCard(mBeaconModel, cardContainer);
            }
            else if (structure instanceof EddystoneTLM) {
                EddystoneTLM eddystoneTlmStructure = (EddystoneTLM) structure;
                mBeaconModel = new BeaconModel(BeaconType.eddystoneTLM);
                mBeaconModel.getEddystoneTLM().setVoltage(eddystoneTlmStructure.getBatteryVoltage());
                mBeaconModel.getEddystoneTLM().setTemperature(eddystoneTlmStructure.getBeaconTemperature());
                mBeaconModel.getEddystoneTLM().setAdvertisingCount(eddystoneTlmStructure.getAdvertisementCount());
                mBeaconModel.getEddystoneTLM().setUptime(eddystoneTlmStructure.getElapsedTime()/100);
                fillEddystoneTLMCard(mBeaconModel, cardContainer);
            }
            else if (structure instanceof EddystoneEID) {
                // TODO This is ugly, I am forced to create an unused BeaconModel just to get the type of the beacon, must be changed
                mBeaconModel = new BeaconModel(BeaconType.eddystoneEID);
                fillEddystoneEIDCard((EddystoneEID)structure, cardContainer);
            }
            else if (structure instanceof LocalName) {
                fillNameCard((LocalName)structure, cardContainer);
            }
            else if (structure instanceof TxPowerLevel) {
                fillTxPowerLevel((TxPowerLevel)structure, cardContainer);
            }
            else if (structure instanceof UUIDs) {
                fillUuidCard((UUIDs)structure, cardContainer);
            }
            else if (structure instanceof ServiceData) {
                fillServiceDataCard((ServiceData)structure, cardContainer);
            }
            else if (structure instanceof Flags) {
                fillFlagsCard((Flags)structure, cardContainer);
            }
            else if (structure instanceof ADManufacturerSpecific) {
                AltBeacon altBeacon = AltBeacon.parseRecord(mScanResult.getScanRecord());
                IBeacon iBeacon = new IBeaconParser().parseScanRecord(mScanResult.getScanRecord());
                if (iBeacon != null) {
                    mBeaconModel = new BeaconModel(BeaconType.ibeacon);
                    mBeaconModel.setIBeacon(iBeacon);
                    fillIBeaconCard(mBeaconModel, cardContainer);
                }
                else if (altBeacon != null) {
                    mBeaconModel = new BeaconModel(BeaconType.altbeacon);
                    mBeaconModel.setAltBeacon(altBeacon);
                    fillAltBeaconCard(mBeaconModel, cardContainer);
                }
                else {
                    fillManufacturerCard((ADManufacturerSpecific)structure, cardContainer);
                }
            }
            else {
                fillUnknownTypeCard(structure, cardContainer);
            }
            appendCardSpace(cardContainer);
        }
        if (mBeaconModel == null) {
            mBeaconModel = new BeaconModel(BeaconType.raw);
        }
    }

    private void appendCardSpace(ViewGroup container) {
        mLayoutInflater.inflate(R.layout.view_space, container);
    }


    private void fillDeviceCard(ViewGroup container) {
        // Counter total size of packet
        int broadcastSize = 0;
        for(ADStructure structure : mAdStructures) {
            broadcastSize += structure.getLength() + 1; // Byte of size and content of byte of size
        }
        View view = mLayoutInflater.inflate(R.layout.card_beacon_device, container, false);
        container.addView(view);
        TextView rssiView = (TextView)view.findViewById(R.id.carddevice_textview_rssi);
        rssiView.setText(getString(R.string.card_device_rssi, mScanResult.getRssi()));
        TextView macView = (TextView)view.findViewById(R.id.carddevice_textview_mac);
        macView.setText(getString(R.string.card_device_mac, mScanResult.getDevice().getAddress()));
        TextView typeView = (TextView)view.findViewById(R.id.carddevice_textview_type);
        String type;
        switch (mScanResult.getDevice().getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "classic";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "dual";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "low energy";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
            default:
                type = "unknown";
        }
        typeView.setText(getString(R.string.card_device_bluetoothtype, type));
        CheckBox scanResponseCheckbox = (CheckBox)view.findViewById(R.id.carddevice_checkbox_scanresponse);
        sLogger.debug("Length of scan record: {}", broadcastSize);
        if (mScanResult.getScanRecord() != null && broadcastSize > 31) {
            scanResponseCheckbox.setChecked(true);
        }
        appendCardSpace(container);
    }


    private void fillIBeaconCard(BeaconModel model, ViewGroup cardContainer) {
        ViewEditIBeacon iBeaconView = new ViewEditIBeacon(getContext());
        iBeaconView.loadModelFrom(model);
        iBeaconView.setEditMode(false);
        cardContainer.addView(iBeaconView);
    }


    private void fillEddystoneURLCard(BeaconModel model, ViewGroup cardContainer) {
        ViewEditEddystoneUrl eddystoneUrlView = new ViewEditEddystoneUrl(getContext());
        eddystoneUrlView.loadModelFrom(model);
        eddystoneUrlView.setEditMode(false);
        cardContainer.addView(eddystoneUrlView);
    }


    private void fillEddystoneUIDCard(BeaconModel model, ViewGroup cardContainer) {
        ViewEditEddystoneUid eddystoneUidView = new ViewEditEddystoneUid(getContext());
        eddystoneUidView.loadModelFrom(model);
        eddystoneUidView.setEditMode(false);
        cardContainer.addView(eddystoneUidView);
    }

    private void fillEddystoneTLMCard(BeaconModel model, ViewGroup cardContainer) {
        ViewEditEddystoneTlm eddystoneTlmView = new ViewEditEddystoneTlm(getContext());
        eddystoneTlmView.loadModelFrom(model);
        eddystoneTlmView.setEditMode(false);
        cardContainer.addView(eddystoneTlmView);
    }

    private void fillEddystoneEIDCard(EddystoneEID eddystoneEid, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_eddystone_eid, cardContainer, false);
        cardContainer.addView(view);
        final TextView txPowerText = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_powervalue);
        final TextView eidValueText = (TextView)view.findViewById(R.id.cardeddystoneeid_textview_value);
        txPowerText.setText(NumberFormat.getInstance().format(eddystoneEid.getTxPower()));
        eidValueText.setText(eddystoneEid.getEIDAsString());
    }

    private void fillAltBeaconCard(BeaconModel model, ViewGroup cardContainer) {
        ViewEditAltBeacon altBeaconView = new ViewEditAltBeacon(getContext());
        altBeaconView.loadModelFrom(model);
        altBeaconView.setEditMode(false);
        cardContainer.addView(altBeaconView);
    }


    private void fillNameCard(LocalName localName, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_name, cardContainer, false);
        cardContainer.addView(view);
        final TextView text = (TextView)view.findViewById(R.id.cardname_textview_name);
        final CheckBox checkbox = (CheckBox)view.findViewById(R.id.cardname_checkbox_fullname);
        text.setText(localName.getLocalName());
        checkbox.setChecked(localName.isComplete());
    }


    private void fillFlagsCard(Flags flags, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_flags, cardContainer, false);
        cardContainer.addView(view);
        final CheckBox limitedDiscoverableCheckbox = (CheckBox)view.findViewById(R.id.cardflags_checkbox_limiteddiscoverable);
        limitedDiscoverableCheckbox.setChecked(flags.isLimitedDiscoverable());
        final CheckBox generalDiscoverableCheckbox = (CheckBox)view.findViewById(R.id.cardflags_checkbox_generaldiscoverable);
        generalDiscoverableCheckbox.setChecked(flags.isGeneralDiscoverable());
        final CheckBox classicUnsupportedCheckbox = (CheckBox)view.findViewById(R.id.cardflags_checkbox_classicunsupported);
        classicUnsupportedCheckbox.setChecked(!flags.isLegacySupported());
        final CheckBox simultaneousControllerCheckbox = (CheckBox)view.findViewById(R.id.cardflags_checkbox_simultaneouscontroller);
        simultaneousControllerCheckbox.setChecked(flags.isControllerSimultaneitySupported());
        final CheckBox simultaneousHostCheckbox = (CheckBox)view.findViewById(R.id.cardflags_checkbox_simultaneoushost);
        simultaneousHostCheckbox.setChecked(flags.isHostSimultaneitySupported());
    }


    private void fillUnknownTypeCard(ADStructure structure, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_unknown, cardContainer, false);
        cardContainer.addView(view);
        final TextView dataSizeValue = (TextView)view.findViewById(R.id.cardunknown_textview_datasize);
        dataSizeValue.setText(NumberFormat.getInstance().format(structure.getLength()));
        final TextView structureTypeValue = (TextView)view.findViewById(R.id.cardunknown_textview_type);
        final String gapType = mBtNumbers.convertGapType(structure.getType());
        if (gapType != null) {
            structureTypeValue.setText(String.format("0x%02X - %s", structure.getType(), gapType));
        }
        else {
            structureTypeValue.setText(String.format("0x%02X", structure.getType()));
        }
        final TextView unknownContent = (TextView)view.findViewById(R.id.cardunknown_textview_content);
        unknownContent.setText(ByteTools.bytesToHexWithSpaces(structure.getData()).toUpperCase());
    }

    private void fillManufacturerCard(ADManufacturerSpecific structure, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_manufacturer, cardContainer, false);
        cardContainer.addView(view);
        final TextView manufacturerIdValue = (TextView)view.findViewById(R.id.cardmanufacturer_textinput_manufacturerid);
        String companyId = String.format("0x%02X", structure.getCompanyId());
        String companyName = mBtNumbers.convertCompanyId(structure.getCompanyId());
        if (companyName != null) {
            companyId += " - " + companyName;
        }
        manufacturerIdValue.setText(companyId);
        if (structure.getLength() <= 2) {
            return;
        }
        final TextView unknownContent = (TextView)view.findViewById(R.id.cardmanufacturer_textview_datacontent);
        unknownContent.setText(
                ByteTools.bytesToHexWithSpaces(
                        Arrays.copyOfRange(structure.getData(),2,structure.getData().length+1)
                ).toUpperCase()
        );
    }


    private void fillTxPowerLevel(TxPowerLevel txPowerLevel, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_txpower, cardContainer, false);
        cardContainer.addView(view);
        final TextView text = (TextView)view.findViewById(R.id.cardtxpower_textview_powervalue);
        text.setText(String.format(Locale.US, "%d dBm", txPowerLevel.getLevel()));
    }


    private void fillServiceDataCard(ServiceData serviceData, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_service, cardContainer, false);
        cardContainer.addView(view);
        String serviceClassValue = "";
        int startOffset = 0;
        switch (serviceData.getType()) {
            case 0x16: // 16-bit UUID
                serviceClassValue = getString(R.string.all_16bit);
                startOffset = 2;
                break;
            case 0x20: // 32-bit UUID
                serviceClassValue = getString(R.string.all_32bit);
                startOffset = 4;
                break;
            case 0x21:  // 128-bit UUID
                serviceClassValue = getString(R.string.all_128bit);
                startOffset = 16;
                break;
        }
        final TextView serviceClass = (TextView)view.findViewById(R.id.cardservice_textview_class);
        serviceClass.setText(serviceClassValue);
        final TextView currentUuid = (TextView)view.findViewById(R.id.cardservice_textinput_uuid);
        final String uuidDescription = mBtNumbers.convertServiceUuid(serviceData.getServiceUUID());
        if (uuidDescription != null) {
            currentUuid.setText(serviceData.getServiceUUID().toString() + "\n" + uuidDescription);
        }
        else {
            currentUuid.setText(serviceData.getServiceUUID().toString());
        }
        final TextView serviceDataContent = (TextView)view.findViewById(R.id.cardservice_textview_datacontent);
        serviceDataContent.setText(
                ByteTools.bytesToHexWithSpaces(
                        Arrays.copyOfRange(serviceData.getData(),startOffset,serviceData.getData().length+1)
                ).toUpperCase()
        );

    }


    private void fillUuidCard(UUIDs uuids, ViewGroup cardContainer) {
        View view = mLayoutInflater.inflate(R.layout.card_beacon_uuids, cardContainer, false);
        cardContainer.addView(view);
        String serviceClassValue = "";
        String serviceTypeValue = "";
        switch (uuids.getType())  {
            // 16-bit UUIDs
            case 0x02: // Incomplete List of 16-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_16bit);
                serviceTypeValue = getString(R.string.card_service_type_incomplete);
                break;
            case 0x03: // Complete List of 16-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_16bit);
                serviceTypeValue = getString(R.string.card_service_type_complete);
                break;
            case 0x14: // List of 16-bit Service Solicitation UUIDs
                serviceClassValue = getString(R.string.all_16bit);
                serviceTypeValue = getString(R.string.card_service_type_sollicitation);
                break;
            // 32-bit UUIDs
            case 0x04: // Incomplete List of 32-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_32bit);
                serviceTypeValue = getString(R.string.card_service_type_incomplete);
                break;
            case 0x05: // Complete List of 32-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_32bit);
                serviceTypeValue = getString(R.string.card_service_type_complete);
                break;
            case 0x1F: // List of 32-bit Service Solicitation UUIDs
                serviceClassValue = getString(R.string.all_32bit);
                serviceTypeValue = getString(R.string.card_service_type_sollicitation);
                break;
            // 128-bit UUIDs
            case 0x06: // Incomplete List of 128-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_128bit);
                serviceTypeValue = getString(R.string.card_service_type_incomplete);
                break;
            case 0x07: // Complete List of 128-bit Service Class UUIDs
                serviceClassValue = getString(R.string.all_128bit);
                serviceTypeValue = getString(R.string.card_service_type_complete);
                break;
            case 0x15: // List of 128-bit Service Solicitation UUIDs
                serviceClassValue = getString(R.string.all_128bit);
                serviceTypeValue = getString(R.string.card_service_type_sollicitation);
                break;
        }
        final TextView serviceClass = (TextView)view.findViewById(R.id.carduuids_textview_class);
        serviceClass.setText(serviceClassValue);
        final TextView serviceType = (TextView)view.findViewById(R.id.carduuids_textview_servicetype);
        serviceType.setText(serviceTypeValue);
        final TextView uuidList = (TextView)view.findViewById(R.id.carduuids_textview_uuidlist);
        final StringBuilder toDisplay = new StringBuilder();
        for(UUID uuid : uuids.getUUIDs()) {
            final String uuidDescription = mBtNumbers.convertServiceUuid(uuid);
            if (uuidDescription != null) {
                toDisplay.append(uuid.toString().toUpperCase()).append(" - ").append(uuidDescription).append("\n");
            }
            else {
                toDisplay.append(uuid.toString().toUpperCase()).append("\n");
            }
        }
        uuidList.setText(toDisplay.toString().trim());
    }

}

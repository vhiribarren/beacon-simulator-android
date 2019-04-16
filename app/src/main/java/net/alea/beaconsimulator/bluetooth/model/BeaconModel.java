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

package net.alea.beaconsimulator.bluetooth.model;

import android.bluetooth.le.AdvertiseSettings;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator;
import net.alea.beaconsimulator.bluetooth.ExtendedAdvertiseData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BeaconModel implements Parcelable {

    private static final Logger sLogger = LoggerFactory.getLogger(BeaconModel.class);


    private UUID id;
    private String name;
    private BeaconType type;
    private Settings settings;

    private EddystoneUID eddystoneUID;
    private EddystoneTLM eddystoneTLM;
    private EddystoneURL eddystoneURL;
    private EddystoneEID eddystoneEID;
    private IBeacon ibeacon;
    private AltBeacon altbeacon;

    private BeaconModel() {}

    private BeaconModel(String name, UUID id, Settings settings) {
        this.name = name;
        this.id = id;
        this.settings = settings;
    }

    public BeaconModel(String name, UUID id, Settings settings, IBeacon ibeacon) {
        this(name, id, settings);
        this.type = BeaconType.ibeacon;
        this.ibeacon = ibeacon;
    }

    public BeaconModel(BeaconType type) {
        this("", UUID.randomUUID(), new Settings());
        this.type = type;
        switch (type) {
            case ibeacon:
                this.ibeacon = new IBeacon();
                break;
            case eddystoneUID:
                this.eddystoneUID = new EddystoneUID();
                break;
            case eddystoneTLM:
                this.eddystoneTLM = new EddystoneTLM();
                break;
            case eddystoneURL:
                this.eddystoneURL = new EddystoneURL();
                break;
            case eddystoneEID:
                this.eddystoneEID = new EddystoneEID();
                break;
            case altbeacon:
                this.altbeacon = new AltBeacon();
                break;
            default:
                sLogger.warn("Beacon type {} is not managed by the BeaconModel", type);
        }

    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BeaconType getType() {
        return type;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public EddystoneUID getEddystoneUID() {
        return eddystoneUID;
    }

    public void setEddystoneUID(EddystoneUID eddystoneUID) {
        this.eddystoneUID = eddystoneUID;
    }

    public EddystoneTLM getEddystoneTLM() {
        return eddystoneTLM;
    }

    public void setEddystoneTLM(EddystoneTLM eddystoneTLM) {
        this.eddystoneTLM = eddystoneTLM;
    }

    public EddystoneURL getEddystoneURL() {
        return eddystoneURL;
    }

    public void setEddystoneURL(EddystoneURL eddystoneURL) {
        this.eddystoneURL = eddystoneURL;
    }

    public EddystoneEID getEddystoneEID() {
        return eddystoneEID;
    }

    public void setEddystoneEID(EddystoneEID eddystoneEID) {
        this.eddystoneEID = eddystoneEID;
    }

    public IBeacon getIBeacon() {
        return ibeacon;
    }

    public void setIBeacon(IBeacon iBeacon) {
        this.ibeacon = iBeacon;
    }

    public AltBeacon getAltbeacon() {
        return altbeacon;
    }

    public void setAltBeacon(AltBeacon altbeacon) {
        this.altbeacon = altbeacon;
    }


    public String generateBeaconName() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return type.name() + "-" + dateFormat.format(new Date());
    }


    public String serializeToJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static BeaconModel parseFromJson(String json) {
        Gson gson = new Gson();
        BeaconModel beacon = null;
        try {
            beacon = gson.fromJson(json, BeaconModel.class);
        }
        catch (JsonSyntaxException e) {
            sLogger.warn("Error while parsing JSON", e);
        }
        return beacon;
    }

    @Override
    public String toString() {
        return serializeToJson();
    }

    public AdvertiseSettings generateADSettings() {
        if (settings != null) {
            return settings.generateADSettings();
        }
        else {
            return null;
        }
    }

    @Nullable
    public ExtendedAdvertiseData generateADData() {
        AdvertiseDataGenerator conversion = null;
        switch (type) {
            case eddystoneTLM:
                conversion = eddystoneTLM;
                break;
            case eddystoneUID:
                conversion = eddystoneUID;
                break;
            case eddystoneURL:
                conversion = eddystoneURL;
                break;
            case eddystoneEID:
                conversion = eddystoneEID;
                break;
            case ibeacon:
                conversion = ibeacon;
                break;
            case altbeacon:
                conversion = altbeacon;
                break;
            case raw:
            default:
                sLogger.warn("Not implemented");
        }
        if (conversion != null) {
            return new ExtendedAdvertiseData(conversion.generateAdvertiseData());
        }
        else {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeParcelable(this.settings, flags);
        dest.writeParcelable(this.eddystoneUID, flags);
        dest.writeParcelable(this.eddystoneTLM, flags);
        dest.writeParcelable(this.eddystoneURL, flags);
        dest.writeParcelable(this.eddystoneEID, flags);
        dest.writeParcelable(this.ibeacon, flags);
        dest.writeParcelable(this.altbeacon, flags);
    }

    protected BeaconModel(Parcel in) {
        this.id = (UUID) in.readSerializable();
        this.name = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : BeaconType.values()[tmpType];
        this.settings = in.readParcelable(Settings.class.getClassLoader());
        this.eddystoneUID = in.readParcelable(EddystoneUID.class.getClassLoader());
        this.eddystoneTLM = in.readParcelable(EddystoneTLM.class.getClassLoader());
        this.eddystoneURL = in.readParcelable(EddystoneURL.class.getClassLoader());
        this.eddystoneEID = in.readParcelable(EddystoneEID.class.getClassLoader());
        this.ibeacon = in.readParcelable(IBeacon.class.getClassLoader());
        this.altbeacon = in.readParcelable(AltBeacon.class.getClassLoader());
    }

    public static final Creator<BeaconModel> CREATOR = new Creator<BeaconModel>() {
        @Override
        public BeaconModel createFromParcel(Parcel source) {
            return new BeaconModel(source);
        }

        @Override
        public BeaconModel[] newArray(int size) {
            return new BeaconModel[size];
        }
    };
}

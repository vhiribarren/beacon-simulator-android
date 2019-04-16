# Beacon Simulator


1. [About this project](#about-this-project)
2. [Configure and build the project](#configure-and-build-the-project)
3. [Removal of iBeacon support](#removal-of-ibeacon-support)
4. [License](#license)
5. [Contributing](#contributing)


## About this project


This is the repository for the Android Beacon Simulator project. It is currently published on the
Play Store at https://play.google.com/store/apps/details?id=net.alea.beaconsimulator

This app transforms your Android device into a virtual BLE beacon advertiser and transmitter.
You can create your own collection of beacon configurations and use them anytime, anywhere,
to emulate a physical beacon.

Its purpose is essentially to help developers working on beacon software with more flexibility than
with some real beacons.

Current features:

- Broadcast and advertise AltBeacon, Eddystone and iBeacon (iBeacon is not part of the open
  source code)
- Eddystone support for: URL, UID, unencrypted TLM (telemetry), EID (Ephemeral ID)
- Create, edit, manage and save your beacon configurations
- Background broadcast
- Scan of nearby beacons, copy of their configuration
- Check tool to know how many broadcast is possible on your phone
- In English and French

Android limitations:

- the MAC address is automatically managed by Android, and will change for each new EID rotation
- not possible to broadcast several different frames with a same MAC address
- broadcast capability depends on your phone, you can use the check tool to know what is possible
  for you
- BLE advertising data flags cannot be added

Other limitations:

- TLM data are static in time
- Scan is not a main feature of the app for now, just an helper

Permissions are for:

- bluetooth: scan, broadcast using Bluetooth
- location: unfortunately, required by Android to perform a Bluetooth scan
- Internet: for the URL minifier


## Configure and build the project


As compared with the project published on the PlayStore, the "community" prefix is added to
avoid app collisions on devices.

Some files need to be added / modified after a clone of the project.


### API Keys

Add a `app/src/main/res/values/api_keys.xml` file containing API keys:

- bitly_generic_token: Bitly code for URL shortener


## Removal of iBeacon support


The support of iBeacon is removed from this open source code.


### Reason

The Apple iBeacon license states: 

> Licensee will not, without Apple's express prior written consent:
> 
> (i) incorporate, combine, or distribute any Licensed Technology, or any derivative
> thereof, with any Public Software, 
> 
> or 
> 
> (ii) use any Public Software in the development of Licensed Products, 
> 
> in such a way that would cause the Licensed Technology, or any derivative
> thereof, to be subject to all or part of the license obligations or other intellectual
> property related terms with respect to such Public Software. As used in this
> subsection, "Public Software" means any software that, as a condition of use,
> copying, modification or redistribution, (a) requires attribution, (b) requires such
> software and derivative works thereof to be disclosed or distributed in source
> code form, or (c) requires such software to be licensed for the purpose of making
> derivative works, or to be redistributed free of charge, commonly referred to as
> free or open source software, including but not limited to software licensed under
> the GNU General Public License, Lesser/Library GPL, Affero GPL, Mozilla Public
> License, Common Public License, Common Development and Distribution
> License, Apache, MIT, or BSD license.

In order to comply with those requirements, the iBeacon Bluetooth low energy advertise data packet
format is removed from this source code.

The code may still contains some strings or data format referring to it (e.g. UUID, major, and
minor numbers) since there are part of public interfaces and knowledge for developers. 


### Adding iBeacon support

Do not write me about the missing code, I will not answer.

If you agree with Apple iBeacon license and want to add iBeacon support, here are some clues
where to add the code. You can do so and publish your work without having to distribute the
iBeacon advertise format, since the license of Beacon Simulator allows this case.

For broadcast:

- `net.alea.beaconsimulator.bluetooth.model.IBeacon::generateAdvertiseData()`
  *(part of the license exception, you can keep this code closed)*
- `layout/dialog_new_beacon_template.xml`: enable visibility of TextView

For scan in order to recognize iBeacon:

- `net.alea.beaconsimulator.bluetooth.IBeaconParser::parseScanResult()`
  *(part of the license exception, you can keep this code closed)*


## License

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 3 of the License, or (at your option) any later
version.

Linking Beacon Simulator statically or dynamically with other modules is making
a combined work based on Beacon Simulator. Thus, the terms and conditions of
the GNU General Public License cover the whole combination.

**As a special exception, the copyright holders of Beacon Simulator give you
permission to combine Beacon Simulator program with free software programs
or libraries that are released under the GNU LGPL and with independent
modules that communicate with Beacon Simulator solely through the
net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the
net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces. You may
copy and distribute such a system following the terms of the GNU GPL for
Beacon Simulator and the licenses of the other code concerned, provided that
you include the source code of that other code when and as the GNU GPL
requires distribution of source code and provided that you do not modify the
net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the
net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces.**

The intent of this license exception and interface is to allow Bluetooth low energy
closed or proprietary advertise data packet structures and contents to be sensibly
kept closed, while ensuring the GPL is applied. This is done by using an interface
which only purpose is to generate android.bluetooth.le.AdvertiseData objects.

This exception is an additional permission under section 7 of the GNU General
Public License, version 3 (“GPLv3”).

Some consequences are:

- if you publish another version on the Play Store or directly distribute a modified
  version to someone elase, you must publish and redistribute the modified source code
- you can integrate a proprietary beacon format without having to publish its advertise
  data packet structure

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.


## Contributing


### For app publication

This is a beacon simulator, not a scanner or can-do-anything tool.

I do not personally to make it evolute so much, so maintenance contributions are welcomed.
If your goal is your contribution to be integrated in the current Beacon Simulator app on
the Play Store, you should contact me before writing some codes and do a pull request.
I will not automatically accept all pull requests.

You also have to accept the current software license, meaning you accept I can publish the
contributed code on the Play Store without me having to give back the code to generate iBeacon
advertise data packets.

If you add or modify a substantial part of the code, do not forget to add yoursef to the
copyright notices at the top of the involved files.
 

### TODO

Known bugs:

- [ ] resilient mode does not work any more when switching off and on Bluetooth
- [ ] broadcast notification is not removed after launching the max broadcast tool

Features:

- [ ] add "connectable" support (according to the Android source code, it add some broadcast Flags
  that cannot be directly added with the Android BLE API)
- [ ] filter capability in scanner to only show beacons, and not all BLE devices

Code refactoring / enhancement:

- [ ] The AdvertiseDataParser and IBeacon parser classes were quickly added to enable the freeing
  of the source code while not having to redistribute the iBeacon parsing method, but a whole
  refactoring should be done to unify the parsing of all beacon types
- [ ] More modular and object oriented approach in order to add new beacons (currently, if/else-if
  code mixed with view components)
- [ ] Refactor Fragments to be more MVP/MVVM/MVWhatever
- [ ] BeaconSimulatorService and App are currently a mess
- [ ] Addition of automated tests if possible...
- [ ] Replace EventBus by something more Rx

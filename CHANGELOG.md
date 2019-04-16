# Change Log
All notable changes to this project will be documented in this file.

## Unreleased
### Added
- GPL license with exception in all files
- new AdvertiseDataParser interface to isolate beacon advertise data parsing in a method

### Changed
- completely rewrite of the README file for public exposure
- check of bitly token presence so the app does not crash if it is not available

### Removed
- all iBeacon code with information coming from Apple Proximity Beacon specifications
- personal logo image

## 1.5.1 - 2019-03-23
### Fixed
- Attempt to solve random crash at service startup, due to startForegroundService/startForeground sequence

## 1.5.0 - 2019-03-10
### Changed
- removed Fabric and Crashlytics due to Google notification of Violation of Usage of
  Android Advertising ID policy and section 4.8 of the Developer Distribution Agreement
- update to SDK API 26 so the app can be deployed on the store again
- replaced Google URL shortener by Bit.ly shortener, due to end of maintenane by Google.

## 1.4.1 - 2017-03-19
### Fixed
- Fix attempt on a crash when binding to service due to getActivity that is null
- Fix attempt on a crash at broadcast startup due to a bad bluetooth state

### Changed
- SLF4J logging system with custom logger

## 1.4.0 - 2017-03-05
### Added
- Resilient mode for broadcast
- More stats on features for Fabric

### Changed
- Full refactoring of resource naming

### Fixed
- Android forgets to remove some broadcasts when Bluetooth is switched off, until no more broadcast can be done
- Flickering problems while updating beacon lists
- Eddystone URL shortener sometimes crash if takes too long
- When modifying a non broadcasting beacon while another beacon is broadcasting, the beacon is triggered on
- Attempt to fix IllegalArumgentException when unbiding from BeaconSimulatorService service


## 1.3.0 - 2017-01-02
### Added
- Support for unencrypted Eddystone TLM frames
- Support for encrypted Eddystone EID frames
- Stop brodcast button in action bar
- Drawer menu
- Settings menu
- Possibility to choose language
- Possible to keep screen on while scanning
- Analysis to find maximal number of broadcastable beacons
- URL shortener for Eddystone URL

### Fixed
- Crash on some badly formed scanned Eddystone beacons
- Random crash depending on Bluetooth state when stopping broadcast

## 1.2.2 - 2016-11-12
### Added
- Version number in about screen

### Fixed
- UUID input helper refuses the deletion of some hyphens
- In detailed scan, some text field have focus on the tip
- Bug in Google Material Design library, random crashes
- Crash when doing a screen rotation while copy bottom sheet is on screen
- Bottom sheet not totally open in scan & Bluetooth query dialogs
- Now scan records are different if they come from a same MAC address but have different contents

## 1.2.1 - 2016-10-18
### Fixed
- In copy beacon, bottom sheet covered by keyboard

## 1.2.0 - 2016-10-16
### Added
- Metric to check if about page is viewed
- More exhaustive data when searching for beacons

### Fixed
- Sometimes a null pointer exception appears when getting manufacturer data for raw BLE broadcast.
- Crash when viewing detailed scan of an Eddystone URL beacon with no embedded URL
- Management of scan and simulator fragments can cause some crashes
- "Home" button was not working in "about" and "detailed scan" screens
- When in powersaving mode, the "scanning" button was all orange
- Expansion problem of bottom sheets in landscape mode
- In beacon edit fragment, some fields had focus color after they where disabled

### Changed
- Better logo for iBeacon, AltBeacon
- HVCT logo in about page
- Interface more compatible with Material Design spec

## 1.1.2 - 2016-07-25
### Rollback
- Removed hyphen fixed, seems to cause crashes and weird behaviors

## 1.1.1 - 2016-07-24
### Added
- Metrics for Fabric Answers: nb of broadcast, copy and scan

### Changed
- No more use of WebView for about screen

### Fixed
- Crash for some provided values in AltBeacon format
- Hyphen sometimes could not be deleted in iBeacon & AltBeacon text input
- Better selection zone to enable/disable broadcast

## 1.1.0 - 2016-06-11
### Added
- AltBeacon support
- transition animation between activities

### Fixed
- Beacon scan now continuing after a screen orientation change
- Random freeze of scanner view, touch events were sometimes discarded by Android root view after
coming back from a beacon detailed view; was due to bad provided transition definition
- Random crash when switching off Bluetooth while broadcasting
- White square icon on some devices for foreground service


## 1.0.1 - 2016-05-15
### Added
- Explanation in edit screens about which value should be put in TxPower
- Animation on FAB when scanning for beacons

### Fixed
- Removed big space that was left when moving a space beacon from top to bottom of list
- List of scan beacons now kept after turning screen
- Edition state now kept after turning screen
- Crash when in edit mode, turning screen, and pushing back button

## 1.0.0 - 2016-05-14
### Added
- First public version.

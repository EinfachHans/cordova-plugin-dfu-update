# Advanced ImagePicker Cordova Plugin
[![npm version](https://badge.fury.io/js/cordova-plugin-dfu-update.svg)](https://badge.fury.io/js/cordova-plugin-dfu-update)

This [Cordova](https://cordova.apache.org) Plugin is a Wrapper to use Nordic Semiconductor's Device Firmware Update (DFU) service to update a Bluetooth LE device.

It currently uses [iOSDFULibrary](https://cocoapods.org/pods/iOSDFULibrary) (Version `4.8.0`) on iOS and 
[Android-DFU-Library](https://github.com/NordicSemiconductor/Android-DFU-Library) (Default-Version `1.9.0`) on Android. 

**This Plugin is in active development!**

<!-- DONATE -->
[![Donate](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG_global.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LMX5TSQVMNMU6&source=url)

This and other Open-Source Cordova Plugins are developed in my free time.
To help ensure this plugin is kept updated, new features are added and bugfixes are implemented quickly, please donate a couple of dollars (or a little more if you can stretch) as this will help me to afford to dedicate time to its maintenance.
Please consider donating if you're using this plugin in an app that makes you money, if you're being paid to make the app, if you're asking for new features or priority bug fixes.
<!-- END DONATE -->

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Content**

- [Install](#install)
  - [Android](#android)
  - [iOS](#ios)
- [Environment Variables](#environment-variables)
  - [Android](#android-1)
  - [iOS](#ios-1)
- [Usage](#usage)
- [Api](#api)
  - [updateFirmware](#updatefirmware)
- [Changelog](#changelog)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Install

## Android

## iOS

This Plugin is developed in Swift and automaticaly adds the Plugin to [Support Swift](https://github.com/akofman/cordova-plugin-add-swift-support).

# Environment Variables

## Android

- ANDROID_NORDIC_VERSION - Version of `no.nordicsemi.android:dfu` / default to `1.9.0` 

## iOS

# Usage

The plugin is available via a global variable named `window.DfuUpdate`.
A TypeScript definition is included out of the Box. You can import it like this:
```ts
import DfuUpdate from 'cordova-plugin-dfu-update';
```

# Api

The list of available methods for this plugin is described below.

## updateFirmware

Start the Firmware-Update proccess

### Parameters:

- fileURL (string) - A string that is the path to the file to use in the update. It can be either in either `cdvfile://` or `file://` format.
- deviceIdentifier (string) - A string that contains the identifier for the Bluetooth LE device to update. It will either be a MAC address (on Android) or a UUID (on iOS).

```js
window.DfuUpdate.updateFirmware(function(success) {
  console.log(success);
}, function (error) {
  console.error(error);
}, 'file_url', 'deviceId');
```

# Changelog

The full Changelog is available [here](CHANGELOG.md)

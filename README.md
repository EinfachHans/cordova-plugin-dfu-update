# Cordova Nordic DFU plugin [![npm version](https://badge.fury.io/js/cordova-plugin-dfu-update.svg)](https://badge.fury.io/js/cordova-plugin-dfu-update)

This plugin allows you to use the Nordic DFU service on your BLE devices to update the firmware. See the [Nordic documentation](https://infocenter.nordicsemi.com/topic/com.nordic.infocenter.sdk5.v12.2.0/index.html) for more details.

## Supported

- Android: fully supported
- iOS: fully supported

Additionally, the device to update must follow the rules as defined in the DFU documentation.

- Supported SDKs: 
  - Android: [v1.9.0](https://github.com/NordicSemiconductor/Android-DFU-Library/tree/v1.9.0)
  - iOS: [v4.4.1](https://github.com/NordicSemiconductor/IOS-Pods-DFU-Library/tree/4.4.1) (Uses Swift)

## Requirements

- Cordova: at least version 9
- Android: Cordova-android of at least 8.0.0

## Installation

run:
`cordova plugin add cordova-plugin-dfu-update`

## API

The API is available as a global `NordicUpdate` object

### Update Firmware

```javascript
DfuUpdate.updateFirmware(function successCallback, function errorCallback, string fileURL, string deviceIdentifier);
```

Params:

- `successCallback`: A function that takes a single argument object. See example later for what this looks like. This will be called multiple times during the update process with different statuses.
- `errorCallback` A function that takes a single argument. The argument will be an error message or an error object.
- `fileURL`: A string that is the path to the file to use in the update. It can be either in either `cdvfile://` or `file://` format.
- `deviceIdentifier`: A string that contains the identifier for the Bluetooth LE device to update. It will either be a MAC address (on Android) or a UUID (on iOS).
   

### Testing

To make it easier to test this plugin, there are two files in the `test-files` folder. 

1. Install the one ending in `.hex` on your nRF52-832 dev board by dragging on dropping it into the board's file system (like you would install any example from the nRF5 SDK). 
1. Put the file ending with `.zip` on your phone in a spot where you know the File URL. 
1. Get the MAC address of the dev board (use nRF Connect and look for a device called "Nordic_Buttonless")
1. Use the File URL and MAC address with this plugin.
1. It should start the DFU process and report progress to the success callback.

### Credits

This plugin was inspired by the work on [this plugin fork](https://github.com/fxe-gear/cordova-plugin-ble-central) by [@fxe-gear](https://github.com/fxe-gear).

Thanks!

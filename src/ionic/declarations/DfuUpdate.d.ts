/// <reference path="./interfaces/update-options.d.ts" />

declare module 'cordova-plugin-dfu-update' {

    export default class DfuUpdate {
        static updateFirmware(resultCallback: (result: any) => void, errorCallback: (error: any) => void, options: UpdateOptions);
    }

}

import iOSDFULibrary

@objc(DfuUpdate) class DfuUpdate : CDVPlugin, CBCentralManagerDelegate, DFUServiceDelegate, DFUProgressDelegate  {

    var dfuCallbackId: String?
    var manager = CBCentralManager()
    var dfuController: DFUServiceController?

    @objc(pluginInitialize)
    override func pluginInitialize() {
        super.pluginInitialize()
        manager = CBCentralManager(delegate: self, queue: nil)
    }

    @objc(updateFirmware:)
    func updateFirmware(command: CDVInvokedUrlCommand) {
        commandDelegate.run {
            self.dfuCallbackId = command.callbackId

            var pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR
            )

            let options = command.argument(at: 0) as? NSDictionary;
            if(options == nil) {
                self.commandDelegate!.send(
                    CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: "The first Argument must be the Configuration"
                    ),
                    callbackId: self.dfuCallbackId
                )
                return;
            }

            let deviceId = options?.value(forKey: "deviceId") as? String
            let fileURL = options?.value(forKey: "fileUrl") as? String;
            let packetReceiptNotificationsValue = options?.value(forKey: "packetReceiptNotificationsValue") as? NSInteger ?? 10;

            if(deviceId == nil) {
                self.commandDelegate!.send(
                    CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: "Device id is required"
                    ),
                    callbackId: self.dfuCallbackId
                )
                return;
            }

            if(fileURL == nil) {
                self.commandDelegate!.send(
                    CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: "File URL is required"
                    ),
                    callbackId: self.dfuCallbackId
                )
                return;
            }

            if deviceId!.count < 1 {

                self.commandDelegate!.send(
                    CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: "Device ID is required"
                    ),
                    callbackId: self.dfuCallbackId

                )
                return
            }

            if (fileURL!.count < 1) {

                self.commandDelegate!.send(
                    CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: "File URL is required"
                    ),
                    callbackId: self.dfuCallbackId
                )
                return
            }

            if (deviceId!.count > 0 && fileURL!.count > 0) {
                let sourceURL = self.getURI(url: fileURL!)

                pluginResult = self.startUpgrade(deviceId: deviceId!, url: sourceURL, packetReceiptNotificationsValue: packetReceiptNotificationsValue);
            }


            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )
        }
    }

    func startUpgrade(deviceId: String, url: URL, packetReceiptNotificationsValue: NSInteger) -> CDVPluginResult {
        let selectedFirmware = DFUFirmware(urlToZipFile: url)

        if (!(selectedFirmware?.valid ?? true)) {
            return CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs: "Invalid firmware"
            )
        }

        let deviceUUID = UUID.init(uuidString: deviceId) ?? nil

        if (deviceUUID == nil) {
            return CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs: "Address " + deviceId + " is not a valid UUID"
            )
        }

        let peripherals = manager.retrievePeripherals(withIdentifiers: [deviceUUID!])
        if (peripherals.count < 1) {
            return CDVPluginResult(
                status: CDVCommandStatus_ERROR,
                messageAs: "Device with address " + deviceId + " not found"
            )
        }

        let deviceP = peripherals[0];

        //let initiator = DFUServiceInitiator(target: deviceP).with(firmware: selectedFirmware!)
        let initiator = DFUServiceInitiator(queue: DispatchQueue(label: "Other"))

        initiator.enableUnsafeExperimentalButtonlessServiceInSecureDfu = true
        initiator.packetReceiptNotificationParameter = UInt16(packetReceiptNotificationsValue)
        initiator.forceDfu = false
        initiator.delegate = self
        initiator.progressDelegate = self

        //dfuController = initiator.start()!
        dfuController = initiator.with(firmware: selectedFirmware!).start(target: deviceP)

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: deviceId + ":" + url.absoluteString
        )

        pluginResult?.setKeepCallbackAs(true)

        return pluginResult!
    }

    func dfuStateDidChange(to state: DFUState) {
        var stateStr: String = "unknown";
        switch(state) {
        case DFUState.connecting:
            stateStr = "deviceConnecting"
            break;
        case DFUState.starting: stateStr = "dfuProcessStarting"; break;
        case DFUState.enablingDfuMode: stateStr = "enablingDfuMode"; break;
        case DFUState.uploading: stateStr = "firmwareUploading"; break;
        case DFUState.validating: stateStr = "firmwareValidating"; break;
        case DFUState.disconnecting: stateStr = "deviceDisconnecting"; break;
        case DFUState.completed: stateStr = "dfuCompleted"; break;
        case DFUState.aborted: stateStr = "dfuAborted"; break;
        }

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: ["status": stateStr]
        )
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: dfuCallbackId)

        if (state == DFUState.aborted || state == DFUState.completed) {
            self.clearHandlers()
        }
    }

    func dfuError(_ error: DFUError, didOccurWithMessage message: String) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR,
            messageAs: [
                "errorMessage": message
            ]
        )

        self.commandDelegate.send(pluginResult, callbackId: dfuCallbackId)
        self.clearHandlers()
    }

    func dfuProgressDidChange(for part: Int, outOf totalParts: Int, to progress: Int, currentSpeedBytesPerSecond: Double, avgSpeedBytesPerSecond: Double) {
        let message = [
            "status": "progressChanged",
            "progress": [
                "percent": progress,
                "speed": currentSpeedBytesPerSecond,
                "avgSpeed": avgSpeedBytesPerSecond,
                "currentPart": part,
                "partsTotal": totalParts,
            ]
            ] as [String : Any]

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: message
        )
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: dfuCallbackId)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {

    }

    func clearHandlers() {
        self.dfuCallbackId = nil
        self.dfuController = nil

        self.pluginInitialize()
    }

    func getURI(url: String) -> URL  {
        var filePath: String = ""
        var resourceURL: NSURL = NSURL.init(string: url)!
        if (url.hasPrefix("cdvfile://")) {
            let filePlugin: CDVFile = commandDelegate.getCommandInstance("File") as! CDVFile
            let url = CDVFilesystemURL.fileSystemURL(with: url)
            filePath = filePlugin.filesystemPath(for: url)
            if (filePath != "") {
                resourceURL = NSURL.init(string: filePath)!
            }
        }

        return resourceURL as URL

    }
}

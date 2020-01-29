var exec = require('cordova/exec');

exports.updateFirmware = function(resultCallback, errorCallback, fileURL, deviceIdentifier) {
	if (typeof resultCallback !== 'function') {
		throw new Error('Result callback must be a function');
	}
	if (typeof deviceIdentifier === 'undefined' && typeof errorCallback !== 'function') {
		deviceIdentifier = fileURL;
		fileURL = errorCallback;
		errorCallback = function(param){};
	}
	exec(resultCallback, errorCallback, "DfuUpdate", "updateFirmware", [deviceIdentifier, fileURL]);
};

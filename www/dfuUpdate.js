var exec = require('cordova/exec');

exports.updateFirmware = function(resultCallback, errorCallback, options) {
	exec(resultCallback, errorCallback, "DfuUpdate", "updateFirmware", [options]);
};

var plugin = function () {
  return window.DfuUpdate;
};
var DfuUpdate = /** @class */ (function () {
  function DfuUpdate() {
  }

  DfuUpdate.updateFirmware = function (resultCallback, errorCallback, options) {
    var plu = plugin();
    return plu.updateFirmware.apply(plu, arguments);
  };
  return DfuUpdate;
}());
export default DfuUpdate;

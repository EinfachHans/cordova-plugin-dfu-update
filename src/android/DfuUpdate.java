package de.einfachhans.DfuUpdate;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DfuUpdate extends CordovaPlugin {

	private final DfuProgressListener progressListener = new DfuProgressListener();

	private static final String TAG = "DfuUpdate";

	private CallbackContext dfuCallback;
	private Activity activity;
	private String deviceAddress;
	private int packetReceiptNotificationsValue;

	private String fileURL;
	private final String COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
	private final String BLUETOOTH = Manifest.permission.BLUETOOTH;
	private final String BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT;
	private final String[] permissions = { COARSE, BLUETOOTH, BLUETOOTH_CONNECT };

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("updateFirmware")) {
			this.updateFirmware(args, callbackContext);
			return true;
		}
		return false;
	}

	private void updateFirmware(JSONArray args, CallbackContext callbackContext) throws JSONException {
		JSONObject options = args.getJSONObject(0);
		String deviceId = options.optString("deviceId");
		String fileURL = options.optString("fileUrl");
		int packetReceiptNotificationsValue = options.optInt("packetReceiptNotificationsValue", 10);

		if (deviceId.equals("")) {
			callbackContext.error("Device id is required");
		}

		if (fileURL.equals("")) {
			callbackContext.error("File URL is required");
		}

		if (!BluetoothAdapter.checkBluetoothAddress(deviceId)) {
			callbackContext.error("Invalid Bluetooth address");
		}

		if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
			callbackContext.error("App must be in the foreground to start DFU");
		}

		this.dfuCallback = callbackContext;
		this.activity = cordova.getActivity();
		this.deviceAddress = deviceId;
		this.fileURL = fileURL;
		this.packetReceiptNotificationsValue = packetReceiptNotificationsValue;

		if (hasPerms()) {
			performUpdateFirmware();
		} else {
			int REQUEST_PERMS_CODE = 234;
			cordova.requestPermissions(this, REQUEST_PERMS_CODE, permissions);
		}
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
			throws JSONException {

		if (hasPerms()) {
			performUpdateFirmware();
		} else {
			this.dfuCallback.error("Permission denied");
		}
	}

	private boolean hasPerms() {
		if (Build.VERSION.SDK_INT >= 31) {
			return cordova.hasPermission(COARSE) && cordova.hasPermission(BLUETOOTH_CONNECT);
		} else {
			return cordova.hasPermission(COARSE) && cordova.hasPermission(BLUETOOTH);
		}
	}

	private void performUpdateFirmware() {
		cordova.getThreadPool().execute(() -> {
			CordovaResourceApi resourceApi = webView.getResourceApi();
			Uri fileUriStr;
			try {
				fileUriStr = resourceApi.remapUri(Uri.parse(fileURL));
			} catch (IllegalArgumentException e) {
				fileUriStr = Uri.parse(fileURL);
			}

			final DfuServiceInitiator starter = new DfuServiceInitiator(deviceAddress)
					.setKeepBond(false)
					.setForceDfu(false)
					.setPacketsReceiptNotificationsEnabled(true)
					.setPacketsReceiptNotificationsValue(packetReceiptNotificationsValue)
					.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
					.setDisableNotification(true);
			starter.setZip(fileUriStr);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				DfuServiceInitiator.createDfuNotificationChannel(cordova.getContext());
			}

			starter.start(activity, DfuService.class);

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			dfuCallback.sendPluginResult(result);

			DfuServiceListenerHelper.registerProgressListener(activity, progressListener);
		});

	}

	private void unregisterDfuProgressListener() {
		DfuServiceListenerHelper.unregisterProgressListener(activity, progressListener);
		dfuCallback = null;
	}

	private class DfuProgressListener extends DfuProgressListenerAdapter {
		@Override
		public void onDeviceConnecting(String deviceAddress) {
			sendDfuNotification("deviceConnecting");
		}

		@Override
		public void onDeviceConnected(String deviceAddress) {
			sendDfuNotification("deviceConnected");
		}

		@Override
		public void onDfuProcessStarting(String deviceAddress) {
			sendDfuNotification("dfuProcessStarting");
		}

		@Override
		public void onDfuProcessStarted(String deviceAddress) {
			sendDfuNotification("dfuProcessStarted");
		}

		@Override
		public void onEnablingDfuMode(String deviceAddress) {
			sendDfuNotification("enablingDfuMode");
		}

		@Override
		public void onFirmwareValidating(String deviceAddress) {
			sendDfuNotification("firmwareValidating");
		}

		@Override
		public void onDeviceDisconnecting(String deviceAddress) {
			sendDfuNotification("deviceDisconnecting");
		}

		@Override
		public void onDeviceDisconnected(String deviceAddress) {
			sendDfuNotification("deviceDisconnected");
		}

		@Override
		public void onDfuCompleted(String deviceAddress) {
			sendDfuNotification("dfuCompleted");
			unregisterDfuProgressListener();
		}

		@Override
		public void onDfuAborted(String deviceAddress) {
			sendDfuNotification("dfuAborted");
			unregisterDfuProgressListener();
		}

		@Override
		public void onError(String deviceAddress, int error, int errorType, String message) {
			JSONObject json = new JSONObject();
			try {
				json.put("id", deviceAddress);
				json.put("error", error);
				json.put("errorType", errorType);
				json.put("message", message);
			} catch (JSONException e) {
				// squelch
			}
			dfuCallback.error(json);
			unregisterDfuProgressListener();
		}

		@Override
		public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart,
				int partsTotal) {
			Log.d(TAG, "sendDfuProgress: " + percent);

			JSONObject json = new JSONObject();
			JSONObject progress = new JSONObject();

			try {
				progress.put("percent", percent);
				progress.put("speed", speed);
				progress.put("avgSpeed", avgSpeed);
				progress.put("currentPart", currentPart);
				progress.put("partsTotal", partsTotal);

				json.put("id", deviceAddress);
				json.put("status", "progressChanged");
				json.put("progress", progress);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
			result.setKeepCallback(true);
			dfuCallback.sendPluginResult(result);
		}

		private void sendDfuNotification(String message) {
			Log.d(TAG, "sendDfuNotification: " + message);

			JSONObject json = new JSONObject();

			try {
				json.put("id", deviceAddress);
				json.put("status", message);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			PluginResult result = new PluginResult(PluginResult.Status.OK, json);
			result.setKeepCallback(true);
			dfuCallback.sendPluginResult(result);
		}
	}
}

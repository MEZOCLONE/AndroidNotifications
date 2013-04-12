package com.matt.remotenotifier;

import org.json.JSONException;
import org.json.JSONObject;

import com.matt.pusher.Pusher;
import com.matt.pusher.PusherConnectionManager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DeviceManagementTask extends AsyncTask<String, Integer, Long> {
	
	private static String TAG = "DeviceManagementTask";
	private Pusher mPusher;
	private String registeredChannelName;
	private JSONObject jObject;
	private final Context ctx;
	
	public DeviceManagementTask(Pusher mPusher, String registeredChannelName, Context ctx){
		this.mPusher = mPusher;
		this.registeredChannelName = registeredChannelName;
		this.ctx = ctx;
		try {
			jObject = new JSONObject("{requestedDevice: all, senderType: controller}");
		} catch (JSONException e) {
			Log.e(TAG, "Error creating jObject", e);
		}
	}

	@Override
	protected Long doInBackground(String... str) {
		Log.d(TAG, "Starting device management task");
		try {
			// Wait before starting the main loop
			synchronized (this) {
				wait(2000);
			}
			while(true){
				PusherConnectionManager.prepare(mPusher, registeredChannelName, ctx, 0);
				Log.i(TAG+" ManagementThread", "Polling for new devices");
				mPusher.sendEvent("client-device_poll_new", jObject, registeredChannelName);
				
				synchronized (this) {
					wait(300000);
				}
			}
		} catch (Exception e) {
			Log.e(TAG+" ManagementThread", "Error occoured on DeviceManagement thread", e);
		}
		return null;
	}
}

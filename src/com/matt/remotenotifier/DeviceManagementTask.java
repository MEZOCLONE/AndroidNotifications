package com.matt.remotenotifier;

import java.io.NotActiveException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.matt.pusher.ChannelEventCoordinator;

public class DeviceManagementTask extends AsyncTask<String, Integer, Long> {
	
	private static String TAG = "DeviceManagementTask";
	private ChannelEventCoordinator channelEventManager;
	private JSONObject jObject;
	
	public DeviceManagementTask(Context ctx){
		try {
			channelEventManager = ChannelEventCoordinator.getInstance();
			jObject = new JSONObject("{requestedDevice: all, senderType: controller}");
		} catch (JSONException e) {
			Log.e(TAG, "Error creating jObject", e);
		} catch (NotActiveException e) {
			Log.w(TAG, e.getMessage());
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
				//PusherConnectionManager.prepare(mPusher, registeredChannelName, ctx, 0);
				Log.i(TAG+" ManagementThread", "Polling for new devices");
				channelEventManager.trigger(0, "client-device_poll_new", jObject.toString());
				
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

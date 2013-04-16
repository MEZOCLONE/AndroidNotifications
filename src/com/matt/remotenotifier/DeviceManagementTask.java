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
	private ChannelEventCoordinator channelEventCoordinator;
	private JSONObject jObject;
	private ChannelEventCoordinator cec;
	
	public DeviceManagementTask(Context ctx){
		try {
			getChannelEventCoordinatorInstance();
			jObject = new JSONObject("{requestedDevice: all, senderType: controller}");
		} catch (JSONException e) {
			Log.e(TAG, "Error creating jObject", e);
		}
	}
	
	private void getChannelEventCoordinatorInstance(){
		if(cec == null){
			try {
				cec = ChannelEventCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "ChannelEventCoordinator is already assigned");
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
				getChannelEventCoordinatorInstance();
				channelEventCoordinator.trigger(0, "client-device_poll_new", jObject.toString());
				
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

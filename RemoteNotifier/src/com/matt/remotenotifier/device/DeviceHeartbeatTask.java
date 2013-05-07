package com.matt.remotenotifier.device;

import java.io.NotActiveException;

import com.matt.pusher.ChannelEventCoordinator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DeviceHeartbeatTask extends AsyncTask<String, Integer, Long> {
	
	private static String TAG = "DeviceHeartbeatTask";
	private ChannelEventCoordinator cec;
	private DeviceCoordinator deviceCoordinator;
	
	public DeviceHeartbeatTask(Context ctx){
		getDeviceCoodinatorInstance();
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
	
	private void getDeviceCoodinatorInstance(){
		if(deviceCoordinator == null){
			try {
				deviceCoordinator = DeviceCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "Device Coodinator is already assigned");
		}
	}

	@Override
	protected Long doInBackground(String... arg0) {
		Log.d(TAG + " HeartbeatThread", "Starting Heartbeat Management Thread");
		final Long HEARTBEAT_TIMEOUT = 180000L;
		try {
			// Wait before starting the main loop
			synchronized (this) {
				wait(2000);
			}
			while (true) {
				if (deviceCoordinator.getDeviceCount() > 0) {
					Log.i(TAG + " HeartbeatThread", "Requesting heatbeats");
					getChannelEventCoordinatorInstance();
					try{
						cec.trigger(0, ChannelEventCoordinator.EVENT_HEARTBEAT_REQUEST, ChannelEventCoordinator.REQUEST_ALL_HEARTBEAT);
						
						synchronized (this) {
							wait(HEARTBEAT_TIMEOUT);
						}

						for (DeviceHolder dh : deviceCoordinator.getDeviceHolderList()) {
							if (dh.getLastHeatbeatTime() < (System.currentTimeMillis() - HEARTBEAT_TIMEOUT)) {
								Log.i(TAG, "Device ["+ dh.getDeviceName()+ "] has not responded to heartbeats in a timely mannor. Removing device");
								deviceCoordinator.deregisterDevice(dh); 
							}
						}
					}catch(Exception e ){
						Log.e(TAG, e.getMessage());
					}

				} else {
					Log.i(TAG + " HeartbeatThread", "No devices registered - Not requesting heartbeats");
				}
				synchronized (this) {
					wait(300000); // Request heartbeats every 5 mins
				}
			}
		} catch (Exception e) {
			Log.e(TAG + " HeartbeatThread", "Error occoured on HeartbeatManagement thread", e);
		}
		return null;
	}

}

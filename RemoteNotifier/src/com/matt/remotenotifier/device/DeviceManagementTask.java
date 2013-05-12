package com.matt.remotenotifier.device;

import java.io.NotActiveException;

import com.matt.pusher.ChannelEventCoordinator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DeviceManagementTask extends AsyncTask<String, Integer, Long> {
	
	private static String TAG = DeviceManagementTask.class.getSimpleName();
	private ChannelEventCoordinator cec;
	
	public DeviceManagementTask(Context ctx){
		
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

				Log.i(TAG+" ManagementThread", "Polling for new devices");
				
				getChannelEventCoordinatorInstance();
				
				try{
					if(cec != null){
						cec.trigger(0, ChannelEventCoordinator.EVENT_POLL_NEW_DEVICE, ChannelEventCoordinator.REQUEST_ALL_NEW_DEVICE);
					}
				}catch(Exception e){
					Log.e(TAG, e.getMessage());
				}
				
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

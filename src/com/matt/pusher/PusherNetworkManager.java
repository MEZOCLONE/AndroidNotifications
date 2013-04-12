package com.matt.pusher;

import java.io.NotActiveException;

import com.matt.remotenotifier.DeviceCoordinator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class PusherNetworkManager extends BroadcastReceiver {
	private DeviceCoordinator deviceCoordinator;
	private static String TAG = "PusherNetworkReceiver";
	
	public PusherNetworkManager(){
		getDeviceCoodinatorInstance();
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

	static boolean isOnline(Context ctx){
		NetworkInfo networkInfo = getNetworkInfo(ctx);
		return (networkInfo != null && networkInfo.isConnected());	
	}

	private static NetworkInfo getNetworkInfo(Context ctx){
		ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo;
	}
	
	private void restartDeviceManagementThreads(){
		getDeviceCoodinatorInstance();
		try {
			Log.i(TAG, "Network reconnected. Restarting device management tasks."); 
			deviceCoordinator.restartDeviceManagementThread();
		} catch (Exception e) {
			Log.w(TAG, "DeviceManagementTask already running");
		}
	}
	
	private void stopDeviceManagementThreads(){
		getDeviceCoodinatorInstance();
		try {
			Log.i(TAG, "Network has been lost. Stopping device management tasks.");
			deviceCoordinator.stopDeviceManagementThread();
		} catch (Exception e) {
			Log.w(TAG, "DeviceManagementTask Error", e);
		}
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		NetworkInfo networkInfo = getNetworkInfo(ctx);
		
	    if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
	        // On WiFi
	    	restartDeviceManagementThreads();
	    } 
	    
	    else if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
	    	// We are on mobile
	    	restartDeviceManagementThreads();
	    } 
	    
	    else {
	    	Log.d(TAG, networkInfo.getTypeName()); 
	    	stopDeviceManagementThreads();
	    }
	    
	}

}

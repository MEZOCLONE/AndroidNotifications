package com.matt.pusher;

import android.content.Context;
import android.util.Log;

import com.matt.remotenotifier.NetworkManager;
import com.pusher.client.Pusher;

public class PusherConnectionManager implements Runnable{
	private String TAG = "PusherConnectionManager";
	private Pusher mPusher;
	private int runMode;
	private ConnectionEventManager connectionEventManager;
	private Context ctx;
	
	public static int MODE_CONNECT_NEW_MANAGER = 0;
	public static int MODE_DISCONNECT = 1;
	public static int MODE_CONNECT = 2;
	
	public PusherConnectionManager(Context ctx, Pusher mPusher, int runMode, String TAG) throws IllegalStateException{
		
		if(runMode == 0){
			throw new IllegalStateException("Run Mode 0 is not allowed from this constructor");
		}
		
		this.ctx = ctx;
		this.mPusher = mPusher;
		this.runMode = runMode;
		
		Log.d(TAG, "PusherConnectionThread called from "+TAG+". Using runMode ["+runMode+"]");
	}
	
	public PusherConnectionManager(Context ctx, Pusher mPusher, int runMode, ConnectionEventManager connectionEventManager, String TAG) {
		
		if(runMode == 0){
			Log.w(TAG, "Run mode 0 requested but not ConnectionEventManager supplied. Running with runMode 2");
			this.runMode = 2;
		}else{
			this.runMode = runMode;
		}
		
		this.ctx = ctx;
		this.mPusher = mPusher;
		this.connectionEventManager = connectionEventManager;
		
		Log.d(TAG, "PusherConnectionThread called from "+TAG+". Using runMode ["+runMode+"]");
	}
	
	@Override
	public void run() {
		if(NetworkManager.isOnline(ctx)){
			switch(runMode){
			case 0:
				// We can call this even if we are connected - calls are ignored unless ConnectionState = DISCONECTED
				mPusher.connect(connectionEventManager);
				break;
				
			case 1:
				mPusher.disconnect();
				break;
			
			case 2:
				mPusher.connect();
				break;
				
			default: 
				Log.e(TAG, "Unknown runMode requested ["+runMode+"]");
				break;
			}
		}else{
			Log.e(TAG, "No network connection to perform this action");
		}

	}

}

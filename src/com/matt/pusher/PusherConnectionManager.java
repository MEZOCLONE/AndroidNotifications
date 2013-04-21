package com.matt.pusher;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import com.matt.remotenotifier.NetworkManager;
import com.pusher.client.Pusher;

public class PusherConnectionManager implements Runnable{
	private static String TAG = "PusherConnectionManager";
	private static Context ctx;
	private static Pusher mPusher;
	private static int runMode;
	
	public static void prepare(Context context, Pusher pusher, int rm, String TAG) throws NetworkErrorException {
		ctx = context;
		mPusher = pusher;
		runMode = rm;
		
		Log.d(TAG, "PusherConnectionThread called from "+TAG+". Using runMode ["+rm+"]");
		if(NetworkManager.isOnline(context)){
			(new Thread(new PusherConnectionManager())).start();
		}else{
			throw new NetworkErrorException("No active network to perform this action");
		}
		
	}
	
	@Override
	public void run() {
		switch(runMode){
		case 0:
			// We can call this even if we are connected - calls are ignored unless ConnectionState = DISCONECTED
			mPusher.connect(new ConnectionEventManager(ctx, mPusher));
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

	}

}

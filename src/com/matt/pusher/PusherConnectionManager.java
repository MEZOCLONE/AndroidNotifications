package com.matt.pusher;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

public class PusherConnectionManager implements Runnable{
	
	private static Pusher mPusher;
	private static String pusherChannel;
	private static String TAG = "PusherConnectionThread";
	private static int runMode;
	
	/**
	 * @param pusher
	 * @param channel
	 * @param context
	 * @param runMode
	 * @throws NetworkErrorException
	 */
	public static void prepare(Pusher pusher, String channel, Context context, int rm) throws NetworkErrorException {
		mPusher = pusher;
		pusherChannel = channel;
		runMode = rm;
		
		Log.d(TAG, "PusherConnectionThread Called. Using runMode ["+rm+"]");
		if(PusherNetworkManager.isOnline(context)){
			(new Thread(new PusherConnectionManager())).start();
		}else{
			throw new NetworkErrorException("No active network to perform this action");
		}
		
		//TODO: After we get network back, we should restart the connection to Pusher and the threads for device management... 
	}
	
	@Override
	public void run() {
		switch(runMode){
		case 0:
			if((!mPusher.isConnected()) && (!mPusher.isConnecting())){
				mPusher.connect();
			}
			break;
			
		case 1:
			mPusher.unbindAll();
			mPusher.unsubscribe(pusherChannel);
			mPusher.disconnect();
			break;
		
		// Force reconnect
		case 2:
			mPusher.unsubscribe(pusherChannel);
			mPusher.disconnect();
			mPusher.connect();
			mPusher.subscribe(pusherChannel);
			break;
			
		default: 
			Log.e(TAG, "Unknown runMode requested ["+runMode+"]");
			break;
		}

	}

}

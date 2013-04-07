package com.matt.remotenotifier;

import android.util.Log;

public class PusherConnectionsThread implements Runnable{
	
	private static Pusher mPusher;
	private static String pusherChannel;
	private static IncomingFragment incomingFragment;
	private static String TAG = "PusherConnectionThread";
	private static int runMode;
	
	public static void prepare(Pusher pusher, String channel, IncomingFragment inf, int rm){
		mPusher = pusher;
		pusherChannel = channel;
		runMode = rm;
		incomingFragment = inf;
		
		Log.d(TAG, "PusherConnectionThread Called. Using runMode ["+rm+"]");
		
		(new Thread(new PusherConnectionsThread())).start();
		
	}

	@Override
	public void run() {
		switch(runMode){
		case 0:
			mPusher.connect();
			break;
		case 1:
			mPusher.unbindAll();
			mPusher.unsubscribe(pusherChannel);
			mPusher.disconnect();
			break;
		default: 
			Log.e(TAG, "Unknown runMode requested");
			break;
		}
		
		if((incomingFragment != null) && (runMode == 0)){
			incomingFragment.showConnectionMessages();
		}
	}

}

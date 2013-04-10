package com.matt.pusher;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class PusherConnectionsThread implements Runnable{
	
	private static Pusher mPusher;
	private static String pusherChannel;
	private static Context ctx;
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
		ctx = context;
		
		Log.d(TAG, "PusherConnectionThread Called. Using runMode ["+rm+"]");
		if(isOnline()){
			(new Thread(new PusherConnectionsThread())).start();
		}else{
			throw new NetworkErrorException("No active network to perform this action");
		}
		
		//TODO: After we get network back, we should restart the connection to Pusher and the threads for device management... 
	}
	
	private static boolean isOnline(){
		ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());	
	}
	
	public static boolean isOnline(Context context){
		ctx = context;
		return isOnline();
	}

	@Override
	public void run() {
		switch(runMode){
		case 0:
			if(!mPusher.isConnected()){
				mPusher.connect();
			}else{
				Log.w(TAG, "Pusher already connected!");
			}
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

	}

}

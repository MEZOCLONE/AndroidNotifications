package com.matt.remotenotifier;

import com.matt.pusher.PusherConnectionManager;
import com.pusher.client.Pusher;
import com.pusher.client.connection.ConnectionState;

import android.accounts.NetworkErrorException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkManager extends BroadcastReceiver {
	private static String TAG = "NetworkManager";
	private Pusher mPusher;
	
	public NetworkManager(Pusher mPusher){
		this.mPusher = mPusher;
	}
	
	public static boolean isOnline(Context ctx){
		NetworkInfo networkInfo = getNetworkInfo(ctx);
		return (networkInfo != null && networkInfo.isConnected());	
	}

	private static NetworkInfo getNetworkInfo(Context ctx){
		ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo;
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		NetworkInfo networkInfo = getNetworkInfo(ctx);
		ConnectionState mPusherState = mPusher.getConnection().getState();
		
	    if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
	       Log.d(TAG, "WiFi Connection");
	       if(mPusherState == ConnectionState.DISCONNECTED){
	    	   PusherConnectionManager pusherConnectionManager = new PusherConnectionManager(ctx, mPusher, PusherConnectionManager.MODE_CONNECT, TAG);
	    	   pusherConnectionManager.run();
	    	}
	    } 
	    
	    else if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
	    	Log.d(TAG, "Mobile Connection");
	    	if(mPusherState == ConnectionState.DISCONNECTED){
	    		PusherConnectionManager pusherConnectionManager = new PusherConnectionManager(ctx, mPusher, PusherConnectionManager.MODE_CONNECT, TAG);
				pusherConnectionManager.run();
	    	}
	    	
	    } 
	    
	    else {
	    	Log.d(TAG, "Lost Connection");
	    }
	    
	}

}

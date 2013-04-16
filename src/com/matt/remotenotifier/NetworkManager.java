package com.matt.remotenotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkManager extends BroadcastReceiver {
	private static String TAG = "NetworkManager";
	
	public NetworkManager(){

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
		
	    if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
	       Log.d(TAG, "WiFi Connection");
	    } 
	    
	    else if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
	    	Log.d(TAG, "Mobile Connection");
	    } 
	    
	    else {
	    	Log.d(TAG, "Lost Connection");
	    }
	    
	}

}

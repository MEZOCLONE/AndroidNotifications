package com.matt.pusher.event;

import android.content.Context;
import android.util.Log;

import com.matt.pusher.PusherConnectionManager;
import com.matt.remotenotifier.NetworkManager;
import com.pusher.client.Pusher;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

/**
 * Management class for handling events that happen on the connection to Pusher
 * @author mattm
 *
 */
public class ConnectionEventManager implements ConnectionEventListener {

	private Context ctx;
	private Pusher mPusher;
	private static final String TAG = "ConnectionEventManager";
	private int connectionRetry  = 1;
	private static int CONNECTION_RETRY_TIMEOUT = 4;
	
	public ConnectionEventManager(Context context, Pusher mPusher){
		this.ctx = context;
		this.mPusher = mPusher;
	}

	@Override
	public void onConnectionStateChange(ConnectionStateChange connectionState) {
		Log.i(TAG, String.format("Pusher Connection State has changed from [%s] to [%s]", connectionState.getPreviousState(), connectionState.getCurrentState()));
		
		if((NetworkManager.isOnline(ctx)) && (connectionState.getCurrentState() == ConnectionState.DISCONNECTED)){
			if(connectionRetry != CONNECTION_RETRY_TIMEOUT){
				Log.i(TAG, "Attempting to reconnect attempt ["+connectionRetry+"]");
				connectionRetry++;
				PusherConnectionManager pusherConnectionManager = new PusherConnectionManager(ctx, mPusher, PusherConnectionManager.MODE_CONNECT, TAG);
				pusherConnectionManager.run();
			}else{
				Log.e(TAG, "Reconnection attempts reached! Use Reconnect to try again");
				connectionRetry = 1;
			}
		}
		
		if(connectionState.getCurrentState() == ConnectionState.CONNECTED){
			connectionRetry = 1;
		}
		
	}

	@Override
	public void onError(String message, String code, Exception e) {
		Log.w(TAG, String.format("An error was received with message [%s], code [%s], exception [%s]", message, code, e));
	}

}

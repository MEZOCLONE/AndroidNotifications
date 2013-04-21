package com.matt.pusher;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import com.matt.remotenotifier.NetworkManager;
import com.pusher.client.Pusher;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class ConnectionEventManager implements ConnectionEventListener {

	private Context ctx;
	private Pusher mPusher;
	private static final String TAG = "PusherConnectionEventListener";
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
				try {
					connectionRetry++;
					PusherConnectionManager.prepare(ctx, mPusher, 2, TAG);
				} catch (NetworkErrorException e) {
					Log.w(TAG, e.getMessage());
				}
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

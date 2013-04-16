package com.matt.pusher;

import android.content.Context;
import android.util.Log;

import com.matt.remotenotifier.NetworkManager;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

public class ConnectionEventManager implements ConnectionEventListener {
	
	private Context ctx;
	private static final String TAG = "PusherConnectionEventListener";
	
	public ConnectionEventManager(Context context){
		this.ctx = context;
	}

	@Override
	public void onConnectionStateChange(ConnectionStateChange connectionState) {
		Log.i(TAG, String.format("Pusher Connection State has changed from [%s] to [%s]", connectionState.getPreviousState(), connectionState.getCurrentState()));
		if(NetworkManager.isOnline(ctx) && connectionState.getCurrentState() == ConnectionState.DISCONNECTED){
			Log.i(TAG, "Reconnection should be possible");
		}
	}

	@Override
	public void onError(String message, String code, Exception e) {
		Log.w(TAG, String.format("An error was received with message [%s], code [%s], exception [%s]", message, code, e));
	}

}

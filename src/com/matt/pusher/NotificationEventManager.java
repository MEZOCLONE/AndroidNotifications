package com.matt.pusher;

import java.io.NotActiveException;

import org.json.JSONObject;

import android.util.Log;

import com.matt.remotenotifier.DeviceCoordinator;
import com.matt.remotenotifier.DeviceType;
import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.R;
import com.pusher.client.channel.PrivateChannelEventListener;

public class NotificationEventManager implements PrivateChannelEventListener {
	
	private static final String TAG = "NotificationEventManager";
	private IncomingFragment incomingFragment;
	private DeviceCoordinator deviceCoordinator;

	public NotificationEventManager(IncomingFragment incomingFragment) {
		this.incomingFragment = incomingFragment;
		Log.i(TAG, "NotificationEventManager started okay");
	}
	
	private void getDeviceCoodinatorInstance(){
		if(deviceCoordinator == null){
			try {
				deviceCoordinator = DeviceCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "Device Coodinator is already assigned");
		}
	}

	@Override
	public void onEvent(String channelName, String eventName, String data) {
		if(eventName.equalsIgnoreCase("device_push_notification")){
			handlePushNotificationEvent(data);
		}
	}

	private void handlePushNotificationEvent(String data) {
		try{
			getDeviceCoodinatorInstance();
			JSONObject eventData = ChannelEventCoordinator.toJsonObject(data);
			String deviceName = eventData.getString("deviceName");
			DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
			// Whoa there, you're probably thinkin' if true! Are you mad! Well, no, I need to replace this with something from the config
			// that I haven't quite made yet. You know the kinda thing.
			if(true){
				if(deviceCoordinator.deviceHolderExists(deviceName, deviceType)){
					String mainText = eventData.getString("mainText");
					String subText = eventData.getString("subText");
					
					Log.i(TAG, "Incoming notification from ["+deviceName+"]");
					Log.d(TAG, "Message: ["+mainText+"] ["+subText+"]");
					Long now = System.currentTimeMillis();
					addItemToNotificationView("["+deviceName+"] "+mainText, subText, R.color.haloLightGreen, 255, now);
				}
			}
		}catch(Exception e){
			Log.w(TAG, "Unable to parse device notification", e);
		}
		
	}

	@Override
	public void onSubscriptionSucceeded(String channelName) {
		Log.d(TAG, "Bound to Notification Events");		
	}

	@Override
	public void onAuthenticationFailure(String arg0, Exception arg1) {
		Log.w(TAG, "Failed to bing Notification Events");		
	}
	
	private void addItemToNotificationView(final String main, final String sub, final int colourResourseId, final int alpha, final Long time){
		incomingFragment.getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				incomingFragment.addItem(main, sub , colourResourseId, alpha, time);
				incomingFragment.notifyDataSetChanged();
			}
		});
	}
	
}

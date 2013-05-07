package com.matt.pusher.event;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONObject;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.matt.pusher.ChannelEventCoordinator;
import com.matt.remotenotifier.BaseNotificationFactory;
import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.R;
import com.matt.remotenotifier.device.DeviceCoordinator;
import com.matt.remotenotifier.device.DeviceType;
import com.matt.remotenotifier.notifications.NotificationEventHolder;
import com.pusher.client.channel.PrivateChannelEventListener;

/**
 * Management class to handle Notification Events on the Private channel. This should be pre-registered with the {@link ChannelEventCoordinator}. 
 * Failure to do this may lead to missed events.  
 * @author mattm
 *
 */
public class NotificationEventManager implements PrivateChannelEventListener  {
	
	private static final String TAG = "NotificationEventManager";
	private IncomingFragment incomingFragment;
	private DeviceCoordinator deviceCoordinator;
	protected static ArrayList<NotificationEventHolder> notificationEventHolderList;

	public NotificationEventManager(IncomingFragment incomingFragment) {
		this.incomingFragment = incomingFragment;
		notificationEventHolderList = new ArrayList<NotificationEventHolder>();
		
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
									
					// Does the user care about notifications?
					// TODO: Add to user prefs
					if(true){
						// TODO: This should use the Event Object used by the notification list. 
						// It needs to be exposed through the manager, but for the moment, do this.
						BaseNotificationFactory notificationFactory = new BaseNotificationFactory(incomingFragment.getActivity());
						NotificationEventHolder neh = new NotificationEventHolder(mainText);
						if(!notificationEventHolderList.contains(neh)){												
							Log.d(TAG, "Building Notification for this event");
							Notification.Builder nBuilder = notificationFactory.buildNotification("["+deviceName+"] "+mainText, subText);
							neh.setmBuilder(nBuilder);
							neh.setSubText(subText);
							
							Log.d(TAG, "Quietly stashing EventTitle");
							notificationEventHolderList.add(neh);
							notificationFactory.showNotification(notificationEventHolderList.indexOf(neh), neh);
						}else{
							Log.d(TAG, "Getting Notification.Builder object to update");
							
							int index = notificationEventHolderList.indexOf(neh);
							neh = notificationEventHolderList.get(index);
							Notification.Builder nBuilder = neh.getmBuilder();
							Notification.Builder newNBuilder = notificationFactory.updateNotification(nBuilder, "["+deviceName+"] "+neh.getmTitle(), neh.getNum(), subText, neh.getSubText());
							neh.updateNum();
							neh.setmBuilder(newNBuilder);
							neh.setSubText(subText);
							
							notificationFactory.showNotification(notificationEventHolderList.indexOf(neh), neh);
						}
					}
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
	
	/**
	 * Inner BroadcastReiever in the {@link NotificationEventManager} to handle the user cancelling the notification. 
	 * @author mattm
	 *
	 */
	public static class NotificationBroadcastEventReciever extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Removing notification as requested");
			if(intent.getAction().equals("cancel_notification")){
				String nTitle = intent.getStringExtra(BaseNotificationFactory.NOTIFICATION_TITLE);
				if(nTitle != null){
					// We are not really going to remove it as if we do, everything in the list will shuffle down and we'll loose
					// the ability to update other notifications.
					// Set the title to a guid
					NotificationEventHolder neh = new NotificationEventHolder(nTitle);
					if(notificationEventHolderList.contains(neh)){
						int index = notificationEventHolderList.indexOf(neh);
						neh = notificationEventHolderList.get(index);
						neh.setTitle(UUID.randomUUID().toString());
						notificationEventHolderList.set(index, neh);
						neh = null;
					}else{
						Log.wtf(TAG, "Notification removal event fired but notification list did not contain that notifications data!");
					}
					
					Log.d(TAG, "Notification has been removed");
				}
			}
		}
	}

}

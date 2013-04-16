package com.matt.pusher;

import java.io.NotActiveException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.matt.remotenotifier.DeviceCoordinator;
import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.R;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

public class ChannelEventCoordinator implements PrivateChannelEventListener {
	private IncomingFragment incomingFragment;
	private DeviceCoordinator deviceCoordinator;
	private ArrayList<PrivateChannel> channelList;
	private ArrayList<String> eventBindList;
	private Context ctx;
	private static String TAG = "ChannelEventManager";
	private static ChannelEventCoordinator instance;

	/**
	 * Constructor for the ChannelEventManager
	 * @param incomingFragment
	 */
	protected ChannelEventCoordinator(IncomingFragment incomingFragment, Context ctx) {
		this.incomingFragment = incomingFragment;
		this.ctx = ctx;
		
		channelList = new ArrayList<PrivateChannel>();
		eventBindList = new ArrayList<String>();
		
		eventBindList.add(0, "register_device");
		eventBindList.add(0, "deregister_device");
		eventBindList.add(0, "device_heartbeat");
		eventBindList.add(0, "device_ack_execute_job");
		eventBindList.add(0, "device_fail_exceute_job");
		eventBindList.add(0, "device_push_notification");
		
		try {
			deviceCoordinator = DeviceCoordinator.getInstance();
		} catch (NotActiveException e) {
			Log.w(TAG, e.getMessage());
		}
		
		Log.i(TAG, "ChannelEventCoordinator started okay");
	}
	
	public static ChannelEventCoordinator getInstance() throws NotActiveException{
		if(instance != null){
			return instance;
		}else{
			throw new NotActiveException("Channel Manager not yet active");
		}
	}
	
	public static ChannelEventCoordinator getInstance(IncomingFragment incomingFragment, Context ctx){
		if(instance != null){
			return instance;
		}else{
			return new ChannelEventCoordinator(incomingFragment, ctx);
		}
	}
	
	public void assignChannelToManager(PrivateChannel pChannel){
		channelList.add(pChannel);
		
		bindToDeviceEvents(pChannel);
		bindToJobEvents(pChannel);
		bindToNotificationEvents(pChannel);
	}
	
	private void bindToDeviceEvents(PrivateChannel pChannel){
		DeviceEventManager deviceEventHandler = new DeviceEventManager(incomingFragment);
		pChannel.bind("register_device", deviceEventHandler);
		pChannel.bind("deregister_device", deviceEventHandler);
		pChannel.bind("device_heartbeat", deviceEventHandler);
	}
	
	private void bindToJobEvents(PrivateChannel pChannel){
		JobEventManager jobEventHandler = new JobEventManager(incomingFragment);
		pChannel.bind("device_ack_execute_job", jobEventHandler);
		pChannel.bind("device_fail_exceute_job", jobEventHandler);
	}
	
	private void bindToNotificationEvents(PrivateChannel pChannel){
		NotificationEventManager notificationEventHandler = new NotificationEventManager(incomingFragment);
		pChannel.bind("device_push_notification", notificationEventHandler);
	}

	@Override
	public void onEvent(String channelName, String eventName, String data) {
		if(!eventBindList.contains(eventName)){
			Log.w(TAG, "Unknown event ["+eventName+"] recieved on channel ["+channelName+"]");
		}
	}

	@Override
	public void onSubscriptionSucceeded(String channelName) {
		Long now = System.currentTimeMillis();
		incomingFragment.hideConnectionMessages();
		addItemToNotificationView("Remote Notifier Connected", "",R.color.haloLightBlue, 255, now);
		try {
			deviceCoordinator.startDeviceManagentThread();
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
	}

	@Override
	public void onAuthenticationFailure(String message, Exception e) {
		Log.w(TAG, "AuthenticationFailure: "+message, e);
		Long now = System.currentTimeMillis();
		addItemToNotificationView("Remote Notifier Connection Failed", "",R.color.haloDarkRed, 255, now);
	}
	
	public void trigger(int channelId, String eventName, String data){
		channelList.get(channelId).trigger(eventName, data);
		
	}
	
	private void addItemToNotificationView(final String main, final String sub, final int colourResourseId, final int alpha, final Long time){
		Activity a = (Activity) ctx;
		a.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				incomingFragment.hideConnectionMessages();
				incomingFragment.addItem(main, sub , colourResourseId, alpha, time);
			}
		});
	}

}

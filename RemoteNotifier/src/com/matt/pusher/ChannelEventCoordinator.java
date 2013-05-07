package com.matt.pusher;

import java.io.NotActiveException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.matt.pusher.event.DeviceEventManager;
import com.matt.pusher.event.JobEventManager;
import com.matt.pusher.event.NotificationEventManager;
import com.matt.remotenotifier.event.EventFragment;
import com.matt.remotenotifier.R;
import com.matt.remotenotifier.device.DeviceCoordinator;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.channel.PrivateChannelEventListener;

/**
 * Class to coordinate all the events that could happen on the private channel. 
 * New events need to be pre-registered with this class, otherwise they are ignored. 
 * 
 * It will also deal with triggering events on the private channel
 * Multiple private channels are supported. 
 * 
 * @author mattm
 *
 */
public class ChannelEventCoordinator implements PrivateChannelEventListener {
	private EventFragment eventFragment;
	private DeviceCoordinator deviceCoordinator;
	private ArrayList<PrivateChannel> channelList;
	private ArrayList<String> eventBindList;
	private Context ctx;
	private static String TAG = "ChannelEventCoordinator";
	private static ChannelEventCoordinator instance;

	/*
	 * Trigger event data statics
	 */
	public static final String REQUEST_ALL_NEW_DEVICE = "{requestedDevice: all, senderType: controller}";
	public static final String REQUEST_ALL_HEARTBEAT = "{requestedDevice: all, senderType: controller}";
	
	
	/*
	 * Trigger event name statics
	 */
	public static final String EVENT_POLL_NEW_DEVICE = "client-device_poll_new";
	public static final String EVENT_HEARTBEAT_REQUEST = "client-heartbeat_request";
	public static final String EVENT_CANCEL_JOB = "client-cancel_job";
	public static final String EVENT_EXECUTE_JOB = "client-execute_job";
	public static final String EVENT_EXECUTE_TIMED_JOB = "client-execute_timed_job";

	/**
	 * Constructor for the ChannelEventManager
	 * @param incomingFragment
	 */
	protected ChannelEventCoordinator(EventFragment incomingFragment, Context ctx) {
		this.eventFragment = incomingFragment;
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
			throw new NotActiveException("Channel Coordinator not yet active");
		}
	}
	
	public static ChannelEventCoordinator getInstance(EventFragment incomingFragment, Context ctx){
		if(instance != null){
			return instance;
		}else{
			return instance = new ChannelEventCoordinator(incomingFragment, ctx);
		}
	}
	
	public void assignChannelToCoordinator(PrivateChannel pChannel) throws IllegalStateException {
		if(channelList.contains(pChannel)){
			throw new IllegalStateException("Channel ["+pChannel.getName()+"] is already assigned to ChannelEventCoordinator");
		}
		Log.d(TAG, "Adding Channel ["+pChannel.getName()+"] to Channel Coordinator");
		channelList.add(pChannel);
		
		for(String event : eventBindList){
			Log.d(TAG, "Binding to event ["+event+"]");
		}
		
		bindToDeviceEvents(pChannel);
		bindToJobEvents(pChannel);
		bindToNotificationEvents(pChannel);
	}
	
	public boolean isAssigned(PrivateChannel pChannel){
		if(channelList.contains(pChannel)){
			return true;
		}else{
			return false;
		}
	}
	
	private void bindToDeviceEvents(PrivateChannel pChannel){
		DeviceEventManager deviceEventHandler = new DeviceEventManager(eventFragment);
		pChannel.bind("register_device", deviceEventHandler);
		pChannel.bind("deregister_device", deviceEventHandler);
		pChannel.bind("device_heartbeat", deviceEventHandler);
	}
	
	private void bindToJobEvents(PrivateChannel pChannel){
		JobEventManager jobEventHandler = new JobEventManager(eventFragment);
		pChannel.bind("device_ack_execute_job", jobEventHandler);
		pChannel.bind("device_fail_exceute_job", jobEventHandler);
	}
	
	private void bindToNotificationEvents(PrivateChannel pChannel){
		NotificationEventManager notificationEventHandler = new NotificationEventManager(eventFragment);
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
		addItemToNotificationView("Remote Notifier Connected", "",R.color.haloLightBlue, 255, now);
		Log.i(TAG, "Subscription succeeded to "+channelName);
		try {
			Log.d(TAG, "Attempting to start device tasks");
			deviceCoordinator.startDeviceHeartbeatTask();
			deviceCoordinator.startDeviceManagentTask();
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
	
	public void trigger(int channelId, String eventName, String data) throws Exception{
		Log.d(TAG, "Request to trigger event ["+eventName+"] on channel "+channelList.get(channelId));
		try{
			channelList.get(channelId).trigger(eventName, data);
		}catch(ArrayIndexOutOfBoundsException e){
			Log.e(TAG, "ChannelId ["+channelId+"] does not exist or not assigned to this coordinator");
			throw new Exception("Channel Not Found on this coordinator");
		}
	}
	
	private void addItemToNotificationView(final String main, final String sub, final int colourResourseId, final int alpha, final Long time){
		Activity a = (Activity) ctx;
		a.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				eventFragment.hideConnectionMessages();
				eventFragment.addItem(main, sub , colourResourseId, alpha, time);
				eventFragment.notifyDataSetChanged();
			}
		});
	}
	
	public static JSONObject toJsonObject(String data) throws JSONException{
		Log.d(TAG, "Incoming eventData ["+data+"]");
		data = data.trim();
		data = data.replace("\\", "");
		data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
		JSONObject eventData = new JSONObject(data);
		
		return eventData;
	}

}

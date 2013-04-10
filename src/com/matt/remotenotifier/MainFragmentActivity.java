package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.matt.pusher.Pusher;
import com.matt.pusher.PusherCallback;
import com.matt.pusher.PusherChannel;
import com.matt.pusher.PusherConnectionsThread;
import com.matt.remotenotifier.DeviceCoordinator.DeviceType;

public class MainFragmentActivity extends FragmentActivity {
	private static String TAG = "MainFragment";
	private Pusher mPusher;
	private AppPreferences appPrefs;
	private IncomingFragment incomingFragment;
	private CommandFragment commandFragment;
	private DeviceCoordinator deviceCoordinator;
	private JobCoordinator jobCoordinator;
	//private static final String PUBLIC_CHANNEL = "matt_sandbox";
	private static final String PRIVATE_CHANNEL = "private-matt_sandbox";

	PagerAdapterManager pagerAdapter;
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_pageviewer);
		
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, IncomingFragment.class.getName()));
		fragments.add(Fragment.instantiate(this, CommandFragment.class.getName()));
		this.pagerAdapter = new PagerAdapterManager(super.getSupportFragmentManager(), fragments);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(pagerAdapter);

		incomingFragment = (IncomingFragment) pagerAdapter.getItem(0);
		commandFragment = (CommandFragment) pagerAdapter.getItem(1);
		
		appPrefs = new AppPreferences(getApplicationContext());
		
		// TODO: Check for empty string
		if (appPrefs.getKey() == null || appPrefs.getSecret() == null) {
			try {
				Intent i = new Intent("com.matt.remotenotifier.SetPreferences");
				startActivityForResult(i, 1);
			} catch (Exception e) {
				Log.d(TAG, e.getLocalizedMessage());
			}
		}
		
		// Create a new connection to Pusher.
		mPusher = new Pusher(appPrefs.getKey(), appPrefs.getSecret());
		
		incomingFragment.showConnectionMessages();
		try {
			PusherConnectionsThread.prepare(mPusher, PRIVATE_CHANNEL, this, 0);
		} catch (NetworkErrorException e) {
			Log.w(TAG, "Can't connect to Pusher", e);
		}
			
		//Once the connection to Pusher has been established, initialise the device coordinator
		deviceCoordinator = DeviceCoordinator.getInstance(mPusher, commandFragment, PRIVATE_CHANNEL);
		jobCoordinator = JobCoordinator.getInstance(mPusher, PRIVATE_CHANNEL, this.getApplicationContext());
		
		try {
			jobCoordinator.restoreJobHolderList(appPrefs.getJobStore());
			incomingFragment.restoreEventList(appPrefs.getEventListStore());
		} catch (NotActiveException e) {
			Log.w(TAG, "Error on JobStore restore", e);
		} catch (Exception e) {
			Log.w(TAG, "First time. Restore was null.");
		}
		
		if(savedInstanceState != null){
			Log.i(TAG, "Resuming Saved Sate");
			if(!savedInstanceState.isEmpty()){
				ArrayList<DeviceHolder> deviceList = (ArrayList<DeviceHolder>) savedInstanceState.getSerializable("deviceList");
				ArrayList<JobHolder> jobList = (ArrayList<JobHolder>) savedInstanceState.getSerializable("jobList");
				if((deviceList == null) || (jobList == null)){
					Log.w(TAG, "SavedInstanceState was not null, but deviceList and jobList are...");					
				}else{
					try {
						Log.i(TAG, "Expecting Device Coordinator active onRestore");
						deviceCoordinator.restoreDeviceHolderList(deviceList);
						Log.i(TAG, "Expecting Job Coordinator active onRestore");
						jobCoordinator.restoreJobHolderList(jobList);
					} catch (NotActiveException e) {
						Log.w(TAG, "Coordinator not active at time of restore", e);
					}
				}
			}
		}else{
			Log.i(TAG, "Saved Instance State was Null");
		}

		// bindAll here so that we receive notifications form the global channel
		mPusher.bindAll(new PusherCallback() {
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) {
				Long now = System.currentTimeMillis();
				Log.d(TAG, "Received " + eventData.toString() + " for event '"+ eventName + "' on channel '" + channelName + "'.");
				
				if (eventName.equalsIgnoreCase("connection_established")) {
						incomingFragment.hideConnectionMessages();
						incomingFragment.addItem("Remote Notifier Connected", "",R.color.haloLightBlue, 255, now);
				} 
				
				if (eventName.equalsIgnoreCase("pusher:Heartbeat")) {
					if (appPrefs.getHeartbeatShow()) {
						incomingFragment.addItem("Heartbeat from Pusher Service", "",R.color.haloDarkBlue, 120, now);
					}
				}
			}
		});

		// Subscribe to our channel and bind to all events on it. We will filter the events ourself.
		PusherChannel mChannel = mPusher.subscribe(PRIVATE_CHANNEL);
		mChannel.bindAll(new PusherCallback() {
			@Override
			public void onEvent(String eventName, JSONObject eventData, String channelName) {
				Long now = System.currentTimeMillis();
				
				if(eventName.equalsIgnoreCase("register_device")){
					try{
						DeviceHolder device = deviceCoordinator.getNewDeviceHolder(eventData.getString("deviceName"),  DeviceType.valueOf(eventData.getString("deviceType")));
						if(!deviceCoordinator.deviceHolderExists(device)){
							if(deviceCoordinator.registerDevice(device) != -1){
								//We do this separately as a device may not have supplied a command list at registration
								deviceCoordinator.addCommandsToDevice(device, eventData);
								incomingFragment.addItem("Device "+device.getDeviceName()+" registered", "", R.color.haloLightOrange, 120, now);
							}
						}else{
							Log.i(TAG, "Device ["+device.getDeviceName()+"] is already registered. Ingorning");
						}
					}catch(Exception e){
						Log.e(TAG, "Unable to parse register_device event ["+e.getLocalizedMessage()+"]");
					}
				}
				
				if(eventName.equalsIgnoreCase("deregister_device")){
					try{
						DeviceHolder device = deviceCoordinator.getDeviceHolder(eventData.getString("deviceName"),  DeviceType.valueOf(eventData.getString("deviceType")));
						if(deviceCoordinator.deviceHolderExists(device)){
							deviceCoordinator.deregisterDevice(device);
							incomingFragment.addItem("Device "+device.getDeviceName()+" unregistered", "", R.color.haloDarkOrange, 120, now);
						}else{
							Log.i(TAG, "Device ["+eventData.getString("deviceName")+"] can not be deregistered as it does not exist. Ignoring.");
						}
					}catch(Exception e){
						Log.e(TAG, "Unable to parse deregister_device event");
					}
					
				}
				
				if(eventName.equalsIgnoreCase("device_heartbeat")){
					try {
						String deviceName = eventData.getString("deviceName");
						DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
						deviceCoordinator.handleHeartbeat(deviceName, deviceType);
					} catch (JSONException e) {
						Log.e(TAG, "Unable to parse device_heartbeat event");
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
					
				}
				
				if(eventName.equalsIgnoreCase("device_ack_execute_job")){
					try{
						String deviceName = eventData.getString("deviceName");
						DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
						int jobId = eventData.getInt("jobId");
						String retval = null;
						try{
							retval = eventData.getString("retval");
						}catch(JSONException e){
							Log.i(TAG, "Job ["+jobId+"] did not return any assoicated data");
						}
						int jobCoodinatorStatus = jobCoordinator.handleJobAck(deviceName, deviceType, jobId, retval);
						String returnedparsedData;
						if (retval != null){
							returnedparsedData = jobCoordinator.getJobRetval(jobId);
						}else{
							returnedparsedData = "";
						}
						switch (jobCoodinatorStatus){
							case 1: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" recieved by "+deviceName, "", R.color.haloLightPurple, 255, now);
									break;
							case 2: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" completed by "+deviceName, returnedparsedData, R.color.haloDarkPurple, 255, now);
									break;
							case 0: incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" caused error on "+deviceName, "", R.color.haloDarkRed, 255, now);
									break;
						}
					}catch(Exception e){
						Log.w(TAG, "Unable to parse device_ack_execute_job event");
						Log.d(TAG, e.getMessage());
					}
				}
				
				if(eventName.equalsIgnoreCase("device_fail_exceute_job")){
					try{
						String deviceName = eventData.getString("deviceName");
						DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
						int jobId = eventData.getInt("jobId");
						if(jobCoordinator.failJob(jobId)){
							incomingFragment.addItem(jobCoordinator.getJobName(jobId)+" caused error on "+deviceName, "", R.color.haloDarkRed, 255, now);
						}						
					}catch(Exception e){
						Log.w(TAG, "Unable to parse device_ack_execute_job event", e);
					}
				}
				
				if(eventName.equalsIgnoreCase("device_push_notification")){
					try{
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
								
								incomingFragment.addItem("["+deviceName+"] "+mainText, subText, R.color.haloLightGreen, 255, now);
							}
						}
					}catch(Exception e){
						Log.w(TAG, "Unable to parse device notification", e);
					}
				}
			}
		});
		
		// Make a call to update the list so the times are displayed correctly
		Thread updateIncomingList = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						synchronized (this) {
							wait(60000);
						}
					} catch (InterruptedException e) {
						Log.d(TAG, e.getMessage());
					}
					runOnUiThread(new Runnable() {
						public void run() {
							incomingFragment.notifyDataSetChanged();
						}
					});
				}
			}
		});
		updateIncomingList.start();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Long now = System.currentTimeMillis();
		switch (item.getItemId()) {
		case R.id.itemSettings:
			try {
				Intent i = new Intent("com.matt.remotenotifier.SetPreferences");
				startActivityForResult(i, 1);
				return true;
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
		case R.id.itemDisconnect:
			if (mPusher.isConnected()) {
				// Connect to the Pusher server in a separate thread and run the
				// UI update back on the UI thread. This is to add compatibility
				// for Android 2.3.2 and above
				// which does not allow call that use network on Main(). Ho Hum.
				Thread pusherConnectThread = new Thread(new Runnable() {
					@Override
					public void run() {
						mPusher.disconnect();
						runOnUiThread(new Runnable() {
							public void run() {
								if (!mPusher.isConnected()) {
									incomingFragment.addItem("Disconnected from Pusher Service","", R.color.haloDarkRed, 255, now);
								} else {
									Toast.makeText(getApplicationContext(),"Unable to disconect from Pusher Services",
									Toast.LENGTH_SHORT).show();
								}
							}
						});

					}
				});
				pusherConnectThread.start();
				return true;
			}
		case R.id.itemReconnect:
			try {
				if (!mPusher.isConnected()) {
					// Connect to the Pusher server in a separate thread
					PusherConnectionsThread.prepare(mPusher, PRIVATE_CHANNEL, this.getApplicationContext(), 0);
					Toast.makeText(getApplicationContext(),"Reconnecting to Pusher Services",Toast.LENGTH_SHORT).show();
					return true;
				}else{
					PusherConnectionsThread.prepare(mPusher, PRIVATE_CHANNEL, this.getApplicationContext(), 1);
					PusherConnectionsThread.prepare(mPusher, PRIVATE_CHANNEL, this.getApplicationContext(), 0);
					Toast.makeText(getApplicationContext(),"Reconnecting to Pusher Services",Toast.LENGTH_SHORT).show();
				}
				return true;
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
		case R.id.itemClearAll:
			try {
				incomingFragment.clearAll();
				incomingFragment.addItem("Previous events have been cleared", "", R.color.haloLightGreen, 255, now);
				return true;
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
		case R.id.itemRequestDevices:
			Thread pusherPollDevicesManual = new Thread(new Runnable() {
				JSONObject jObject = null;
				@Override
				public void run() {
					try {
						jObject = new JSONObject("{requestedDeviceTypes: all, senderType: controller}");
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					}
					mPusher.sendEvent("client-device_poll_new", jObject, PRIVATE_CHANNEL);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							incomingFragment.addItem("Manual poll for devices started","",R.color.haloLightPurple, 255, now);
						}
					});
				}
			});
			pusherPollDevicesManual.start();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		ArrayList<DeviceHolder> deviceList = new ArrayList<DeviceHolder>();
		ArrayList<JobHolder> jobList = new ArrayList<JobHolder>();
		try {
			Log.d(TAG, "Expecting Device Coordinator active onPause");
			deviceList = deviceCoordinator.getDeviceHolderList();
			Log.d(TAG, "Expecting Job Coordinator active onPause");
			jobList = jobCoordinator.getJobHolderList();
			
			Log.d(TAG, "Writing Jobs and Devices to savedInstanceState");
			savedInstanceState.putSerializable("deviceList", deviceList);
			savedInstanceState.putSerializable("jobList", jobList);
			
		} catch (NotActiveException e) {
			Log.e(TAG, "Coordinator not active at time of pause", e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
			incomingFragment.saveEventList(appPrefs);
			jobCoordinator.shutdown(appPrefs);
			deviceCoordinator.shutdown();
			
			PusherConnectionsThread.prepare(mPusher, PRIVATE_CHANNEL, this.getApplicationContext(), 1);
		}catch(NotActiveException e){
			Log.e(TAG, "Error on coordinator shutdown()", e);
		} catch (NetworkErrorException e) {
			Log.w(TAG, "Can't connect to Pusher", e);
		}		
		Log.i(TAG, "RemoteNotifier Destroyed");
	}
}

package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.matt.pusher.ChannelEventCoordinator;
import com.matt.pusher.ConnectionEventManager;
import com.matt.pusher.PusherConnectionManager;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.connection.Connection;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.util.HttpAuthorizer;

public class MainFragmentActivity extends FragmentActivity {
	private static String TAG = "MainFragment";
	private Pusher mPusher;
	private AppPreferences appPrefs;
	private IncomingFragment incomingFragment;
	private CommandFragment commandFragment;
	private DeviceCoordinator deviceCoordinator;
	private JobCoordinator jobCoordinator;
	private ChannelEventCoordinator channelEventCoordinator;
	private ConnectionEventManager connectionEventManager;
	private NetworkManager receiver;
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
		pagerAdapter = PagerAdapterManager.getInstance(super.getSupportFragmentManager(), fragments);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(pagerAdapter);

		incomingFragment = (IncomingFragment) pagerAdapter.getItem(0);
		commandFragment = (CommandFragment) pagerAdapter.getItem(1);
		
		appPrefs = new AppPreferences(getApplicationContext());
		
		if (appPrefs.getKey() == null || appPrefs.getKey().length() <= 0) {
			try {
				Intent i = new Intent("com.matt.remotenotifier.SetPreferences");
				startActivityForResult(i, 1);
			} catch (Exception e) {
				Log.d(TAG, e.getLocalizedMessage());
			}
		}
		
		// Create a new connection to Pusher.
		HttpAuthorizer auth = new HttpAuthorizer("http://mansion.entrydns.org:8080/");
		PusherOptions opts = new PusherOptions().setAuthorizer(auth);
		mPusher = new Pusher(appPrefs.getKey(), opts);
				
		try {
			jobCoordinator.restoreJobHolderList(appPrefs.getJobStore());
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
		
		connectionEventManager = new ConnectionEventManager(getApplicationContext(), mPusher);
		PusherConnectionManager pcm = new PusherConnectionManager(getApplicationContext(), mPusher, PusherConnectionManager.MODE_CONNECT_NEW_MANAGER, connectionEventManager, TAG);
		pcm.run();
		
		Log.d(TAG, "Registering PusherNetworkReceiver intent");
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkManager(mPusher);
        this.registerReceiver(receiver, filter);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		try {
			
			deviceCoordinator = DeviceCoordinator.getInstance(this);
			jobCoordinator = JobCoordinator.getInstance(this);
			
			channelEventCoordinator = ChannelEventCoordinator.getInstance(incomingFragment, this);
			PrivateChannel pChannel = mPusher.subscribePrivate(PRIVATE_CHANNEL, channelEventCoordinator);
			channelEventCoordinator.assignChannelToCoordinator(pChannel);
			
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
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
		PusherConnectionManager pusherConnectionManager;
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
			pusherConnectionManager = new PusherConnectionManager(this, mPusher, PusherConnectionManager.MODE_CONNECT, TAG);
			pusherConnectionManager.run();
			incomingFragment.addItem("Disconnected from Pusher Service","", R.color.haloDarkRed, 255, now);
			return true;
			
		case R.id.itemReconnect:
			pusherConnectionManager = new PusherConnectionManager(this, mPusher, PusherConnectionManager.MODE_DISCONNECT, TAG);
			pusherConnectionManager.run();
			
			pusherConnectionManager = new PusherConnectionManager(this, mPusher, PusherConnectionManager.MODE_CONNECT, TAG);
			pusherConnectionManager.run();
			
			Toast.makeText(getApplicationContext(),"Reconnecting to Pusher Services",Toast.LENGTH_SHORT).show();
			return true;
				
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
						channelEventCoordinator.trigger(0, "client-device_poll_new", jObject.toString());
					} catch (JSONException e) {
						Log.e(TAG, e.getMessage());
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
		
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
			if(receiver != null){
				Log.d(TAG, "Unregistering Network Broadcast Reveiver");
				this.unregisterReceiver(receiver);
			}
			jobCoordinator.shutdown(appPrefs);
			deviceCoordinator.shutdown();
			
			Log.d(TAG, "Unbinding connectionEventManager");
			mPusher.getConnection().unbind(ConnectionState.ALL, connectionEventManager);
			PusherConnectionManager pcm = new PusherConnectionManager(getApplicationContext(), mPusher, PusherConnectionManager.MODE_DISCONNECT, TAG);
			pcm.run();
		}catch(NotActiveException e){
			Log.e(TAG, "Error on coordinator shutdown()", e);
		}		
		Log.i(TAG, "RemoteNotifier Destroyed");
	}
}

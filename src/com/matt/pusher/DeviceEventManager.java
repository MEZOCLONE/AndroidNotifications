package com.matt.pusher;

import java.io.NotActiveException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.matt.remotenotifier.DeviceCoordinator;
import com.matt.remotenotifier.DeviceHolder;
import com.matt.remotenotifier.DeviceType;
import com.matt.remotenotifier.IncomingFragment;
import com.matt.remotenotifier.R;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;

// Maybe in the future I could use Gson to deserialize the Json straight to the DeviceHolder object. But that's for another time...
public class DeviceEventManager implements PrivateChannelEventListener  {

	private static final String TAG = "RegisterDeviceEventHandler";
	private DeviceCoordinator deviceCoordinator;
	private IncomingFragment incomingFragment;
	
	public DeviceEventManager(IncomingFragment incomingFragment){
		this.incomingFragment = incomingFragment;
	}
	
	@Override
	public void onEvent(String channelName, String eventName, String data) {
		getDeviceCoodinatorInstance();
		
		if(eventName.equalsIgnoreCase("register_device")){
			handleRegisterDeviceEvent(data);
		}
		if(eventName.equalsIgnoreCase("deregister_device")){
			handleDeregisterDeviceEvent(data);
		}
		
		if(eventName.equalsIgnoreCase("device_heartbeat")){
			handleDeviceHeartbeat(data);
		}
		
	}
	
	private void handleDeviceHeartbeat(String data) {
		try {
			JSONObject eventData = new JSONObject(data);
			String deviceName = eventData.getString("deviceName");
			DeviceType deviceType = DeviceType.valueOf(eventData.getString("deviceType"));
			deviceCoordinator.handleHeartbeat(deviceName, deviceType);
		} catch (JSONException e) {
			Log.e(TAG, "Unable to parse device_heartbeat event");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void handleDeregisterDeviceEvent(String data) {
		try{
			JSONObject eventData = new JSONObject(data);
			DeviceHolder device = deviceCoordinator.getDeviceHolder(eventData.getString("deviceName"),  DeviceType.valueOf(eventData.getString("deviceType")));
			if(deviceCoordinator.deviceHolderExists(device)){
				deviceCoordinator.deregisterDevice(device);
				Long now = System.currentTimeMillis();
				incomingFragment.addItem("Device "+device.getDeviceName()+" unregistered", "", R.color.haloDarkOrange, 120, now);
			}else{
				Log.i(TAG, "Device ["+eventData.getString("deviceName")+"] can not be deregistered as it does not exist. Ignoring.");
			}
		}catch(Exception e){
			Log.e(TAG, "Unable to parse deregister_device event");
		}
	}

	private void handleRegisterDeviceEvent(String data) {
		try{
			JSONObject eventData = new JSONObject(data);
			DeviceHolder device = deviceCoordinator.getNewDeviceHolder(eventData.getString("deviceName"),  DeviceType.valueOf(eventData.getString("deviceType")));
			if(!deviceCoordinator.deviceHolderExists(device)){
				if(deviceCoordinator.registerDevice(device) != -1){
					//We do this separately as a device may not have supplied a command list at registration
					deviceCoordinator.addCommandsToDevice(device, eventData);
					Long now = System.currentTimeMillis();
					incomingFragment.addItem("Device "+device.getDeviceName()+" registered", "", R.color.haloLightOrange, 120, now);
				}
			}else{
				Log.i(TAG, "Device ["+device.getDeviceName()+"] is already registered. Ingorning");
			}
		}catch(Exception e){
			Log.e(TAG, "Unable to parse register_device event ["+e.getLocalizedMessage()+"]");
		}
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
	public void onSubscriptionSucceeded(String channelName) {
		Log.d(TAG, "Bound to Device Events");		
	}

	@Override
	public void onAuthenticationFailure(String arg0, Exception arg1) {
		Log.w(TAG, "Failed to bind to Device Events");
	}
	
}

package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/*
 * Singleton instance - Use getInstance 
 */
public class DeviceCoordinator {
	private static final String TAG = "DeviceCoordinator";
	private Integer deviceCount;
	private ArrayList<DeviceHolder> deviceList;
	private OutgoingFragment outgoingFragment;
	private String registeredChannelName;
	private static DeviceCoordinator instance; 
	private JSONObject jObject = null;
	private Pusher mPusher;
	
	public enum DeviceType{
		ARDUINO, RASPBERRYPI, CONTROLLER;
	}
	
	static public DeviceCoordinator getInstance() throws NotActiveException{
		if(instance != null){
			return instance;
		}else{
			throw new NotActiveException("Device Coordinator not yet active");
		}
	}
	
	static public DeviceCoordinator getInstance(Pusher pusher, OutgoingFragment outgoingFragment, String registeredChannelName){
		if(instance != null){
			return instance;
		}else{
			return instance = new DeviceCoordinator(pusher, outgoingFragment, registeredChannelName);
		}
	}
	
	protected DeviceCoordinator(Pusher pusher, OutgoingFragment outgoingFragment, String registeredChannelName) {
		deviceCount = 0;
		deviceList = new ArrayList<DeviceHolder>();
		this.registeredChannelName = registeredChannelName;
		// Start the management threads. One looks after all heatbeats to the devices, the other is handle polling devices
		deviceManagementThread(pusher, registeredChannelName);
		deviceHeatbeatThread(pusher, registeredChannelName);
		this.outgoingFragment = outgoingFragment;
		mPusher = pusher;
		try {
			jObject = new JSONObject("{requestedDevice: all, senderType: controller}");
			Log.d(TAG, "Device Coodinator Started Okay");
		} catch (JSONException e) {
			Log.e(TAG, "Error creating jObject", e);
		}
	}
	
	public boolean deviceHolderExists(String deviceName, DeviceType deviceType){
		DeviceHolder device = new DeviceHolder(this, deviceName, deviceType);
		return deviceList.contains(device);
	}
	
	public boolean deviceHolderExists(DeviceHolder device){
		return deviceList.contains(device);
	}
	
	public int registerDevice(String deviceName, DeviceType deviceType){
		DeviceHolder device = new DeviceHolder(this, deviceName, deviceType);
		if(!deviceList.contains(device)){
			Log.i(TAG, "Registering device ["+device.getDeviceName()+"]");
			deviceList.add(device);
			// We can't control controllers, that would be insane.
			deviceCount++;
			updateControl();
			Log.i(TAG, "Device ["+device.getDeviceName()+"] registered okay");
			return deviceList.indexOf(device);
		}else{
			Log.w(TAG, "Device ["+device.getDeviceName()+"] already registered. Ignoring.");
			return -1;
		}
	}
	
	public int registerDevice(DeviceHolder device){
		if(!deviceList.contains(device)){
			Log.i(TAG, "Registering device ["+device.getDeviceName()+"]");
			deviceList.add(device);
			// We can't control controllers, that would be insane.
			deviceCount++;
			updateControl();
			Log.i(TAG, "Device ["+device.getDeviceName()+"] registered okay");
			return deviceList.indexOf(device);
		}else{
			Log.w(TAG, "Device ["+device.getDeviceName()+"] already registered. Ignoring.");
			return -1;
		}
	}
	
	public void addCommandsToDevice(DeviceHolder device, JSONObject commandList) throws JSONException{
		Log.i(TAG, "Adding commands to device");
		device.addCommand(commandList);
		updateControl();
	}
	
	public void addCommandsToDevice(DeviceHolder device, String name, String command) throws JSONException{
		device.addCommand(name, command);
	}
	
	public int getDeviceCount(){
		return deviceCount;
	}
	
	public int getDeviceIndex(DeviceHolder device){
		return deviceList.indexOf(device);
	}
	
	public DeviceHolder getDeviceHolder(int index){
		return deviceList.get(index);
	}
	
	public DeviceHolder getDeviceHolder(String deviceName, DeviceType deviceType){
		DeviceHolder dhNew = new DeviceHolder(this, deviceName, deviceType);
		if(getDeviceCount() > 0){
			for(DeviceHolder dh : deviceList) {
				if(dh.equals(dhNew)){
					return dh;
				}
			}
		}else{
			return null;
		}
		return null;
	}
	
	public DeviceHolder getNewDeviceHolder(String deviceName, DeviceType deviceType){
		return new DeviceHolder(this, deviceName, deviceType);
	}
	
	public void deregisterDevice(DeviceHolder device){
		Log.i(TAG, "Deregistering device ["+device.getDeviceName()+"]");
		deviceList.remove(device);
		deviceCount--;
		updateControl();
	}
	
	public void deregisterDevice(int index){
		Log.i(TAG, "Deregistering device ["+getDeviceHolder(index).getDeviceName()+"]");
		deviceList.remove(index);
		deviceCount--;
		updateControl();
	}
	
	public void updateControl(){
		Log.d(TAG, "Refreshing control list");
		outgoingFragment.getActivity().runOnUiThread(new Runnable() {	
			@Override
			public void run() {
				outgoingFragment.mAdaptor.notifyDataSetChanged();				
			}
		});
	}
	
	public void requestHeartbeatFromDevice(final String deviceName){
		Thread requestHeatbeatThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jObj = new JSONObject("{requestedDevice: "+deviceName+", senderType: controller}");
					Log.i(TAG, "Requesting heatbeat from "+deviceName);
					mPusher.sendEvent("client-heartbeat_request", jObj, registeredChannelName);
				} catch (JSONException e) {
					Log.w(TAG, "Error requesting heartbeat from device "+deviceName, e);
				}
			}
		});
		requestHeatbeatThread.start();
	}
	
	public void handleHeartbeat(String deviceName, DeviceType deviceType) throws Exception{
		DeviceHolder dh = getDeviceHolder(deviceName, deviceType);
		if(dh == null){
			throw new Exception("Unable to handle heartbeat - Device ["+deviceName+"] does not exist");
		}else{
			dh.touchHeartbeatTime();
			Log.i(TAG, "Heartbeat revieved okay from ["+deviceName+"]");
		}
	}
	
	private void deviceHeatbeatThread(final Pusher mPusher, final String registeredChannelName) {
		Thread deviceHeartbeatThread = new Thread(new Runnable() {
			final Long HEARTBEAT_TIMEOUT = 180000L;
			
			@Override
			public void run() {
				Log.d(TAG + " HeartbeatThread", "Starting Heartbeat Management Thread");
				try {
					// Wait before starting the main loop
					synchronized (this) {
						wait(2000);
					}
					while (true) {
						if (getDeviceCount() > 0) {
							Log.i(TAG + " HeartbeatThread", "Requesting heatbeats");
							mPusher.sendEvent("client-heartbeat_request", jObject, registeredChannelName);
							synchronized (this) {
								wait(HEARTBEAT_TIMEOUT);
							}

							for (DeviceHolder dh : deviceList) {
								if (dh.getLastHeatbeatTime() < (System.currentTimeMillis() - HEARTBEAT_TIMEOUT)) {
									Log.i(TAG, "Device ["+ dh.getDeviceName()+ "] has not responded to heartbeats in a timely mannor. Removing device");
									deregisterDevice(dh); 
								}
							}

						} else {
							Log.i(TAG + " HeartbeatThread", "No devices registered - Not requesting heartbeats");
						}
						synchronized (this) {
							wait(300000); // Request heartbeats every 5 mins
						}
					}
				} catch (Exception e) {
					Log.e(TAG + " HeartbeatThread", "Error occoured on HeartbeatManagement thread", e);
				}

			}
		});
		deviceHeartbeatThread.start();
	}
	
	private void deviceManagementThread(final Pusher mPusher, final String registeredChannelName){
		Thread deviceManagementThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG+" ManagementThread", "Starting device management thread");
				//TODO: Instead of true, use a boolean from prefs
				try {
					// Wait before starting the main loop
					synchronized (this) {
						wait(2000);
					}
					while(true){
						//In the future add in a max number of devices. People could pay to remove this limit.
						Log.i(TAG+" ManagementThread", "Polling for new devices");
						mPusher.sendEvent("client-device_poll_new", jObject, registeredChannelName);
						
						synchronized (this) {
							wait(300000);
						}
					}
				} catch (Exception e) {
					Log.e(TAG+" ManagementThread", "Error occoured on DeviceManagement thread", e);
				}
			}
		});
		deviceManagementThread.start();
	}

}

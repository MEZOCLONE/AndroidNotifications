package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.matt.pusher.ChannelEventCoordinator;

/*
 * Singleton instance - Use getInstance 
 */
public class DeviceCoordinator {
	private static final String TAG = "DeviceCoordinator";
	private static DeviceCoordinator instance;
	private Integer deviceCount;
	private ArrayList<DeviceHolder> deviceList;
	private Context ctx;
	private DeviceManagementTask dmt;
	private DeviceHeartbeatTask dht;
	private ChannelEventCoordinator cec;
	
	static public DeviceCoordinator getInstance() throws NotActiveException{
		if(instance != null){
			Log.d(TAG, "DeviceCoordinator already active. Returning instace");
			return instance;
		}else{
			throw new NotActiveException("Device Coordinator not yet active");
		}
	}
	
	static public DeviceCoordinator getInstance(Context ctx){
		if(instance != null){
			Log.d(TAG, "DeviceCoordinator already active. Returning instace");
			return instance;
		}else{
			return instance = new DeviceCoordinator(ctx);
		}
	}
	
	protected DeviceCoordinator(Context ctx) {
		deviceCount = 0;
		deviceList = new ArrayList<DeviceHolder>();
		this.ctx = ctx;
		getChannelEventCoordinatorInstance();
		Log.i(TAG, "Device Coodinator Started Okay");
	}
	
	public void shutdown() throws NotActiveException {
		if(instance != null){
			Log.d(TAG, "DeviceCoordinator shutting down.");
			
			if(dmt != null){
				dmt.cancel(true);
			}
			
			instance = null;			
		}else{
			Log.w(TAG, "Shutdown requested but not yet active. This can only be called when not active!");
			throw new NotActiveException("Device Coordinator not yet active");
		}
	}
	
	private void getChannelEventCoordinatorInstance(){
		if(cec == null){
			try {
				cec = ChannelEventCoordinator.getInstance();
			} catch (NotActiveException e) {
				Log.w(TAG, e.getMessage());
			}
		}else{
			Log.d(TAG, "ChannelEventCoordinator is already assigned");
		}
	}
	
	public ArrayList<DeviceHolder> getDeviceHolderList() throws NotActiveException{
		if(instance != null){
			return deviceList;
		}else{
			throw new NotActiveException("Device Coordinator not yet active");
		}
		
	}
	
	public void restoreDeviceHolderList(ArrayList<DeviceHolder> deviceList) throws NotActiveException{
		if(instance != null){
			this.deviceList = deviceList;
			deviceCount = deviceList.size();
			updateControl();
		}else{
			throw new NotActiveException("Device Coordinator not yet active");
		}
	}
	
	public boolean deviceHolderExists(String deviceName, DeviceType deviceType){
		DeviceHolder device = new DeviceHolder(deviceName, deviceType);
		return deviceList.contains(device);
	}
	
	public boolean deviceHolderExists(DeviceHolder device){
		return deviceList.contains(device);
	}
	
	public int registerDevice(String deviceName, DeviceType deviceType){
		DeviceHolder device = new DeviceHolder(deviceName, deviceType);
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
		DeviceHolder dhNew = new DeviceHolder(deviceName, deviceType);
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
		return new DeviceHolder(deviceName, deviceType);
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
		final Activity act = (Activity) ctx;
		act.runOnUiThread(new Runnable() {	
			@Override
			public void run() {
				PagerAdapterManager pam;
				try {
					pam = PagerAdapterManager.getInstance();
					CommandFragment commandFragment = (CommandFragment) pam.getItem(1);
					commandFragment.mAdaptor.notifyDataSetChanged();
				} catch (NotActiveException e) {
					Log.w(TAG, "Error getting PagerAdapterManager");
				}				
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
					cec.trigger(0, "client-heartbeat_request", jObj.toString());
				} catch (JSONException e) {
					Log.w(TAG, "Error requesting heartbeat from device "+deviceName, e);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
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
	
	@SuppressLint("NewApi")
	private void deviceHeatbeatTask() {
		// These need to be pooled
		dht = new DeviceHeartbeatTask(ctx);
		dht.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void startDeviceHeartbeatTask() throws Exception{
		if(instance == null){
			throw new NotActiveException("DeviceCoordinator not yet active");
		}
		
		if(dht == null){
			deviceHeatbeatTask();
		}else{
			throw new Exception("DeviceManagementTask already running");
		}
	}
	
	public void stopDeviceHeartbeatTask() throws Exception {
		if(instance == null){
			throw new NotActiveException("DeviceCoordinator not yet active");
		}
		if(dht != null){
			dht.cancel(true);
			dht = null;
		}else{
			throw new Exception("DeviceManagementTask not started");
		}
	}
	
	@SuppressLint("NewApi")
	private void deviceManagementTask(){
		dmt = new DeviceManagementTask(ctx);
		dmt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	public void startDeviceManagentTask() throws Exception{
		if(instance == null){
			throw new NotActiveException("DeviceCoordinator not yet active");
		}
		
		if(dmt == null){
			deviceManagementTask();
		}else{
			throw new Exception("DeviceManagementTask already running");
		}
	}
	
	public void stopDeviceManagementTask() throws Exception {
		if(instance == null){
			throw new NotActiveException("DeviceCoordinator not yet active");
		}
		if(dmt != null){
			dmt.cancel(true);
			dmt = null;
		}else{
			throw new Exception("DeviceManagementTask not started");
		}
	}

}

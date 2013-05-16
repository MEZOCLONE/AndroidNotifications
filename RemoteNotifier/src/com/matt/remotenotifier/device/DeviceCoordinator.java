package com.matt.remotenotifier.device;

import java.io.NotActiveException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.matt.pusher.ChannelEventCoordinator;
import com.matt.remotenotifier.CommandFragment;
import com.matt.remotenotifier.PagerAdapterManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Singleton instance - Use getInstance 
 */
public class DeviceCoordinator {
	private static final String TAG = DeviceCoordinator.class.getSimpleName();
	private static DeviceCoordinator instance;
	private Integer deviceCount;
	private ArrayList<DeviceHolder> deviceList;
	private Context ctx;
	private DeviceDao deviceDao;
	private CommandDao commandDao;
	private ArgumentDao argumentDao;
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
		deviceDao = new DeviceDao(this.ctx);
		commandDao = new CommandDao(this.ctx);
		argumentDao = new ArgumentDao(this.ctx);
		//restoreDeviceHolderList();
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
	
	private void restoreDeviceHolderList(){
		if(instance != null){
			deviceList.clear();
			deviceList = deviceDao.getAllDevices();
			deviceCount = deviceList.size();
			
			for(DeviceHolder device : deviceList){
				device.restoreCommandList(commandDao.getAllCommands(device));
				
				for(CommandHolder command : device.getCommandList()){
					command.restoreArgumentList(argumentDao.getAllArguments(command));
				}
			}
			updateControl();
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
			deviceList.add(deviceDao.createDevice(device));
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
			deviceList.add(deviceDao.createDevice(device));
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
	
	public void configureDevice(DeviceHolder device, JSONObject config) throws JSONException {
		// Do this so we make sure we are working with the one in the list, not the instance passed in
		device = getDeviceHolder(device.getDeviceName(), device.getDeviceType());
		
		addCommandsToDevice(device, config);
	}
	
	protected void addCommandsToDevice(DeviceHolder device, JSONObject commandList) throws JSONException{	
		Log.i(TAG, "Adding commands to device");
		JSONArray commandArray = commandList.getJSONArray("commands");
		for(int i=0; i < commandArray.length(); ++i){
			JSONObject j = commandArray.getJSONObject(i);
			CommandHolder command = commandDao.createCommand(device, j.getString("name"),j.getString("com"));
			addArgumentsToCommand(command, j);
			device.addCommand(command);
		}
		updateControl();
	}
	
	protected void addArgumentsToCommand(CommandHolder command, JSONObject argumentList) throws JSONException{
		JSONArray argArray;
		try{
			argArray = argumentList.getJSONArray("args");
		}catch(JSONException e){
			Log.w(TAG, "No arguments found for command ["+argumentList.getString("name")+"]");
			return;
		}
		Log.d(TAG, "Arguments found for command ["+argumentList.getString("name")+"]");
		for(int i=0; i < argArray.length(); ++i){
			JSONObject j = argArray.getJSONObject(i);
			Log.d(TAG, "Adding ["+j.getString("name")+"] with type ["+j.getString("type")+"]");
			ArgumentHolder arg = argumentDao.createArgument(command, j.getString("name"),j.getString("type"));
			command.addArgument(arg);
		}
	}
	
	public void addCommandsToDevice(DeviceHolder device, String name, String command) throws JSONException{
		CommandHolder commandHolder = commandDao.createCommand(device, name, command);
		device.addCommand(commandHolder);
	}
	
	public void addArgumentsToCommand(CommandHolder command, String name, String type){
		ArgumentHolder argument = new ArgumentHolder(name, type);
		command.addArgument(argument);
	}
	
	public void setArgumentValue(DeviceHolder device, CommandHolder command, int argumentId, String argumentValue){
		command.setArgumentValue(argumentId, argumentValue);
		argumentDao.setValue(command.getArgument(argumentId), argumentValue);
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
		deviceDao.destroyDevice(device);
		deviceCount--;
		updateControl();
	}
	
	public void deregisterDevice(int index){
		Log.i(TAG, "Deregistering device ["+getDeviceHolder(index).getDeviceName()+"]");
		deviceList.remove(index);
		deviceDao.destroyDevice(getDeviceHolder(index));
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
					commandFragment.refreshCommandFragment();
				} catch (NotActiveException e) {
					Log.w(TAG, "Error getting PagerAdapterManager");
				}				
			}
		});
	}
	
	public void requestHeartbeatFromDevice(final String deviceName){
		getChannelEventCoordinatorInstance();
		Thread requestHeatbeatThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jObj = new JSONObject("{requestedDevice: "+deviceName+", requestedDeviceTag: "+ChannelEventCoordinator.DEVICE_TAG+", senderType: controller}");
					Log.i(TAG, "Requesting heatbeat from "+deviceName);
					cec.trigger(0, ChannelEventCoordinator.EVENT_HEARTBEAT_REQUEST, jObj.toString());
				} catch (JSONException e) {
					Log.w(TAG, "Error creating Json Object", e);
				} catch (Exception e) {
					Log.e(TAG, "Error requesting heartbeat from device "+deviceName, e);
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
			deviceDao.updateHeartbeatTime(dh);
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

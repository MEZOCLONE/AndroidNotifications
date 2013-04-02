package com.matt.remotenotifier;

import java.io.NotActiveException;
import java.util.ArrayList;

import org.json.JSONArray;
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
	
	class DeviceHolder{
				
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((deviceName == null) ? 0 : deviceName.hashCode());
			result = prime * result
					+ ((deviceType == null) ? 0 : deviceType.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DeviceHolder other = (DeviceHolder) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (deviceName == null) {
				if (other.deviceName != null)
					return false;
			} else if (!deviceName.equals(other.deviceName))
				return false;
			if (deviceType != other.deviceType)
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "DeviceHolder [deviceName=" + deviceName + "]";
		}

		private String deviceName;
		private DeviceType deviceType;
		private ArrayList<CommandHolder> commandListing;
		private Long lastHeatbeatTime;
		private boolean hasHeartbeat = false;
		
		public DeviceHolder(String deviceName, DeviceType deviceType){
			this.deviceName = deviceName;
			this.deviceType = deviceType;
			lastHeatbeatTime = System.currentTimeMillis();
			commandListing = new ArrayList<CommandHolder>();
		}

		public String getDeviceName() {
			return deviceName;
		}
		
		public DeviceType getDeviceType(){
			return deviceType;
		}
			
		private DeviceCoordinator getOuterType() {
			return DeviceCoordinator.this;
		}
		
		protected void addCommand(JSONObject commandList) throws JSONException{
			JSONArray commandArray = commandList.getJSONArray("commands");
			for(int i=0; i < commandArray.length(); ++i){
				JSONObject j = commandArray.getJSONObject(i);
				CommandHolder com = new CommandHolder(j.getString("name"),j.getString("com"));
				commandListing.add(com);
				com.addArguments(j);
			}
		}
		
		protected void addCommand(String name, String command){
			CommandHolder com = new CommandHolder(name, command);
			commandListing.add(com);
		}
		
		public int getCommandCount(){
			return commandListing.size();
		}
		
		public CommandHolder getCommandHolder(int i){
			return commandListing.get(i);
		}
		
		public String getCommndName(int i){
			return commandListing.get(i).getName();
		}
		
		public String getCommand(int i){
			return commandListing.get(i).getCommand();
		}
		
		public Long getLastHeatbeatTime() {
			return lastHeatbeatTime;
		}

		public void touchHeartbeatTime(){
			lastHeatbeatTime = System.currentTimeMillis();
			hasHeartbeat = true;
		}

		public boolean hasHeartbeat() {
			return hasHeartbeat;
		}
	}
	
	class CommandHolder{
		private String name;
		private String command;
		private ArrayList<ArgumentHolder> argumentListing;
		private ArrayList<Integer> associatedJobIds;
		
		public CommandHolder(String name, String command){
			this.name = name;
			this.command = command;
			argumentListing = new ArrayList<ArgumentHolder>();
		}

		public String getName() {
			return name;
		}

		public String getCommand() {
			return command;
		}
		
		public String getArgumentName(int i){
			return argumentListing.get(i).getArgName();
		}
		
		public String getArgumentType(int i){
			return argumentListing.get(i).getArgType();
		}
		
		public String getArgumentValue(int i){
			return argumentListing.get(i).getArgValue();
		}
		
		public void setArgumentValue(int i, String argValue){
			argumentListing.get(i).setArgValue(argValue);
		}
		
		public int getArgsCount(){
			return argumentListing.size();
		}
		
		public void addJobId(int jobId){
			if(associatedJobIds == null){
				associatedJobIds = new ArrayList<Integer>();
			}
			associatedJobIds.add(jobId);
		}
		
		public boolean isAssociatedToJob(int jobId){
			return associatedJobIds.contains(jobId);
		}
		
		public int getAssociatedJobId(int index){
			return associatedJobIds.get(index);
		}
		
		public int getAssociatedJobCount(){
			if(associatedJobIds == null){
				return 0;
			}
			return associatedJobIds.size();
		}
		
		protected void addArguments(JSONObject commandObj) throws JSONException{
			JSONArray argArray;
			try{
				argArray = commandObj.getJSONArray("args");
			}catch(JSONException e){
				Log.w(TAG, "No arguments found for command ["+commandObj.getString("name")+"]");
				return;
			}
			Log.d(TAG, "Arguments found for command ["+commandObj.getString("name")+"]");
			for(int i=0; i < argArray.length(); ++i){
				JSONObject j = argArray.getJSONObject(i);
				Log.d(TAG, "Adding ["+j.getString("name")+"] with type ["+j.getString("type")+"]");
				ArgumentHolder com = new ArgumentHolder(j.getString("name"),j.getString("type"));
				argumentListing.add(com);
			}
		}
	}
	
	class ArgumentHolder{
		private String argType;
		private String argName;
		private String argValue;
		
		public ArgumentHolder(String argName, String argType){
			this.argName = argName;
			this.argType = argType;
			argValue = ""; // Create an empty string just as a holder
		}
		
		public String getArgName(){
			return argName;
		}
		
		public String getArgType(){
			return argType;
		}
		
		public String getArgValue(){
			return argValue;
		}
		
		public void setArgValue(String argValue){
			this.argValue = argValue;
		}
	}

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

package com.matt.remotenotifier.device;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DeviceHolder implements Serializable{

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (deviceType != other.deviceType)
			return false;
		return true;
	}

	private static final long serialVersionUID = 4944535428907433023L;
	private String deviceName;
	private DeviceType deviceType;
	private ArrayList<CommandHolder> commandListing;
	private Long lastHeatbeatTime;
	private boolean hasHeartbeat = false;

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DeviceHolder [deviceName=" + deviceName + "]";
	}
	
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
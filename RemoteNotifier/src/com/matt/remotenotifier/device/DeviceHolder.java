package com.matt.remotenotifier.device;

import java.io.Serializable;
import java.util.ArrayList;


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
	private Long id;
	private String deviceName;
	private DeviceType deviceType;
	private ArrayList<CommandHolder> commandList;
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
		commandList = new ArrayList<CommandHolder>();
	}

	public String getDeviceName() {
		return deviceName;
	}
	
	public DeviceType getDeviceType(){
		return deviceType;
	}
	
	protected void addCommand(String name, String command){
		CommandHolder com = new CommandHolder(name, command);
		commandList.add(com);
	}
	
	protected void addCommand(CommandHolder command){
		commandList.add(command);
	}
	
	protected void restoreCommandList(ArrayList<CommandHolder> commandList){
		this.commandList.clear();
		this.commandList = commandList;
	}
	
	public int getCommandCount(){
		return commandList.size();
	}
	
	public CommandHolder getCommandHolder(int i){
		return commandList.get(i);
	}
	
	public String getCommndName(int i){
		return commandList.get(i).getName();
	}
	
	public String getCommand(int i){
		return commandList.get(i).getCommand();
	}
	
	protected ArrayList<CommandHolder> getCommandList(){
		return commandList;
	}
	
	public Long getLastHeatbeatTime() {
		return lastHeatbeatTime;
	}
	
	public Long getId(){
		return id;
	}

	public void touchHeartbeatTime(){
		lastHeatbeatTime = System.currentTimeMillis();
		hasHeartbeat = true;
	}

	public boolean hasHeartbeat() {
		return hasHeartbeat;
	}
	
	public void setId(Long Id){
		this.id = Id;
	}
}
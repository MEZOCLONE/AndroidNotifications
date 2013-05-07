package com.matt.remotenotifier.device;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class CommandHolder implements Serializable {
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argumentListing == null) ? 0 : argumentListing.hashCode());
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		CommandHolder other = (CommandHolder) obj;
		if (argumentListing == null) {
			if (other.argumentListing != null)
				return false;
		} else if (!argumentListing.equals(other.argumentListing))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	private static final long serialVersionUID = 4560648720023407165L;
	private static final String TAG = "DeviceCoordinator - CommandHolder";
	private String name;
	private String command;
	private ArrayList<ArgumentHolder> argumentListing;
	private ArrayList<Integer> associatedJobIds;
	
	public CommandHolder(String name, String command){
		this.name = name;
		this.command = command;
		argumentListing = new ArrayList<ArgumentHolder>();
		associatedJobIds = new ArrayList<Integer>();
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
		associatedJobIds.add(jobId);
	}
	
	public boolean isAssociatedToJob(int jobId){
		return associatedJobIds.contains(jobId);
	}
	
	public int getAssociatedJobId(int index){
		return associatedJobIds.get(index);
	}
	
	public int getAssociatedJobCount(){
		return associatedJobIds.size();
	}
	
	public ArrayList<Integer> getAssoicatedJobList() {
		return associatedJobIds;
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
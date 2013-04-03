package com.matt.remotenotifier;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


class CommandHolder{
	private static final String TAG = "DeviceCoordinator - CommandHolder";
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
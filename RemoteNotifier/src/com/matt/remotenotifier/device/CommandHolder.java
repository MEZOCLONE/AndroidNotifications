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
				+ ((argumentList == null) ? 0 : argumentList.hashCode());
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
		if (argumentList == null) {
			if (other.argumentList != null)
				return false;
		} else if (!argumentList.equals(other.argumentList))
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
	private static final String TAG = CommandHolder.class.getSimpleName();
	private Long id;
	private String name;
	private String command;
	private ArrayList<ArgumentHolder> argumentList;
	private ArrayList<Integer> associatedJobIds;
	
	public CommandHolder(String name, String command){
		this.name = name;
		this.command = command;
		argumentList = new ArrayList<ArgumentHolder>();
		associatedJobIds = new ArrayList<Integer>();
	}

	public String getName() {
		return name;
	}

	public String getCommand() {
		return command;
	}
	
	public String getArgumentName(int i){
		return argumentList.get(i).getArgName();
	}
	
	public String getArgumentType(int i){
		return argumentList.get(i).getArgType();
	}
	
	public String getArgumentValue(int i){
		return argumentList.get(i).getArgValue();
	}
	
	public ArgumentHolder getArgument(int index){
		return argumentList.get(index);
	}
	
	protected void setArgumentValue(int argumentId, String argumentValue){
		argumentList.get(argumentId).setArgValue(argumentValue);
	}
	
	protected void setId(Long id){
		this.id = id;
	}
	
	public int getArgsCount(){
		return argumentList.size();
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
	
	public Long getId(){
		return id;
	}
	
	public ArrayList<Integer> getAssoicatedJobList() {
		return associatedJobIds;
	}
	
	protected void restoreArgumentList(ArrayList<ArgumentHolder> argumentList){
		this.argumentList.clear();
		this.argumentList = argumentList;
	}
	
	protected void addArgument(String name, String type){
		ArgumentHolder arg = new ArgumentHolder(name, type);
		argumentList.add(arg);
	}
	
	protected void addArgument(ArgumentHolder argument){
		argumentList.add(argument);
	}
	
}
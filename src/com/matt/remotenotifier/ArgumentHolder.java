package com.matt.remotenotifier;

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
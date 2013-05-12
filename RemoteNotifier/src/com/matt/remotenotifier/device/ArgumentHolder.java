package com.matt.remotenotifier.device;

import java.io.Serializable;

class ArgumentHolder implements Serializable {

	private static final long serialVersionUID = -4356288395022804837L;
	private Long id;
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
	
	public Long getId(){
		return id;
	}
	
	public void setArgValue(String argValue){
		this.argValue = argValue;
	}
	
	public void setId(Long id){
		this.id = id;
	}
}
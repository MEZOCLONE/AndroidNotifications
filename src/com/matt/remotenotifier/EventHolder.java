package com.matt.remotenotifier;

import java.io.Serializable;

class EventHolder implements Serializable {

	private static final long serialVersionUID = -102973054041517660L;
	private String textMain;
	private String textSub;
	private int iconResource;
	private int alphaData;
	private Long timeData;
	
	public EventHolder(String textMain, String textSub, int iconResource, int alphaData, Long timeData){
		this.textMain = textMain;
		this.textSub = textSub;
		this.iconResource = iconResource;
		this.alphaData = alphaData;
		this.timeData = timeData;
	}	

	/**
	 * @return the textMain
	 */
	public String getTextMain() {
		return textMain;
	}

	/**
	 * @return the textSub
	 */
	public String getTextSub() {
		return textSub;
	}

	/**
	 * @return the iconResource
	 */
	public int getIconResource() {
		return iconResource;
	}

	/**
	 * @return the alphaData
	 */
	public int getAlphaData() {
		return alphaData;
	}

	/**
	 * @return the timeData
	 */
	public Long getTimeData() {
		return timeData;
	}


	/**
	 * @param textMain the textMain to set
	 */
	public void setTextMain(String textMain) {
		this.textMain = textMain;
	}


	/**
	 * @param textSub the textSub to set
	 */
	public void setTextSub(String textSub) {
		this.textSub = textSub;
	}


	/**
	 * @param iconResource the iconResource to set
	 */
	public void setIconResource(int iconResource) {
		this.iconResource = iconResource;
	}
	

}

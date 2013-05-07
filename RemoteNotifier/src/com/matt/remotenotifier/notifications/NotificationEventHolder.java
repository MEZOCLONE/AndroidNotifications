package com.matt.remotenotifier.notifications;

import android.app.Notification;

public class NotificationEventHolder{
	private String nTitle;
	private String subText;
	private Notification.Builder mBuilder;
	int num;
	
	public NotificationEventHolder(String nTitle) {
		//this.mBuilder = mBuilder;
		this.nTitle = nTitle;
		num = 1;
	}
	
	public void setmBuilder(Notification.Builder mBuilder){
		this.mBuilder = mBuilder;
	}

	public String getmTitle() {
		return nTitle;
	}
	
	public String getSubText() {
		return subText;
	}

	public Notification.Builder getmBuilder() {
		return mBuilder;
	}
	
	public int getNum(){
		return num;
	}
	
	public void updateNum(){
		num++;
	}

	public void setSubText(String subText) {
		this.subText = subText;
	}
	
	public void setTitle(String nTitle){
		this.nTitle = nTitle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nTitle == null) ? 0 : nTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationEventHolder other = (NotificationEventHolder) obj;
		if (nTitle == null) {
			if (other.nTitle != null)
				return false;
		} else if (!nTitle.equals(other.nTitle))
			return false;
		return true;
	}

}
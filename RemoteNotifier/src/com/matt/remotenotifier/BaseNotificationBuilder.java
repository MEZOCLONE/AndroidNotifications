package com.matt.remotenotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Address;
import android.util.Log;

public class BaseNotificationBuilder {
	private static final String TAG = BaseNotificationBuilder.class.getSimpleName();
	private Context context;
	
	public BaseNotificationBuilder(Context context){
		this.context = context;
	}
	
	/**
	 * Base builder for a notification
	 * 
	 * @param nId
	 * @param nTitle
	 * @param nText
	 */
	public Notification.Builder buildNotification(String nTitle, String nText){
		Log.d(TAG, "Building notification with title ["+nTitle+"]");
		Notification.Builder mBuilder = new Notification.Builder(context)
        	.setSmallIcon(android.R.drawable.ic_dialog_info)
        	.setContentTitle(nTitle)
        	.setContentText(nText);
		return mBuilder;
	}
	
	public Notification.Builder updateNotification(Notification.Builder nBuilder, String mainTitle, int nNumber, String... items){
		Log.d(TAG, "Updating notification with Id");
		String summaryText;
		if(nNumber <= 1){
			summaryText = "";
		}else{
			summaryText = "+ "+(nNumber-1)+" more";
		}
		nBuilder.setContentTitle(mainTitle)
			.setContentText(items[0])
			.setNumber(nNumber)
			.setStyle(new Notification.InboxStyle()
				.addLine(items[0])
				.addLine(items[1])
				.setSummaryText(summaryText));
		
		return nBuilder;
	}
	
	public int showNotification(int nId, Notification.Builder nNotrification){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(nId, nNotrification.build());
		
		return nId;		
	}
	
	

}
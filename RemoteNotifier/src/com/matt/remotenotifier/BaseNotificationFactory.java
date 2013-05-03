package com.matt.remotenotifier;

import com.matt.pusher.NotificationEventManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Base class for building and updating Notifications. These can then be shown to the user by calling showNotification. 
 * This class does not track the notifications if produces, this is up to the manager that calls this class.
 * 
 * To create bespoke notifications for a specific event type, extend this class and override the builder methods
 * @author mattm
 *
 */
public class BaseNotificationFactory {
	private static final String TAG = BaseNotificationFactory.class.getSimpleName();
	private Context context;
	
	public BaseNotificationFactory(Context context){
		this.context = context;
	}
	
	/**
	 * Create a Notification.Builder object with the given parameters. During the creation of this object, setAutoCancel is set true.
	 * 
	 * @param nTitle
	 * @param nText
	 * @return The Notification.Builder object
	 */
	public Notification.Builder buildNotification(String nTitle, String nText){
		Log.d(TAG, "Building notification with title ["+nTitle+"]");
		Notification.Builder mBuilder = new Notification.Builder(context)
        	.setSmallIcon(android.R.drawable.ic_dialog_info)
        	.setContentTitle(nTitle)
        	.setContentText(nText)
        	.setDeleteIntent(getDeleteIntent());
        	//.setAutoCancel(false);
		return mBuilder;
	}
	
	/**
	 * Create a Notification.Builder object that supports a progress update. 
	 * This method will automatically set setAutoCancel = false and setOngoing = true
	 * @param nTitle
	 * @param nText
	 * @return The Notification.Builder object
	 */
	public Notification.Builder buildProgressNotification(String nTitle, String nText){
		Log.d(TAG, "Building progress notification with title ["+nTitle+"]");
		Notification.Builder nBuilder = new Notification.Builder(context)
			.setSmallIcon(android.R.drawable.ic_popup_sync)
			.setContentTitle(nTitle)
			.setContentText(nText)
			.setAutoCancel(false)
			.setOngoing(true)
			.setDeleteIntent(getDeleteIntent())
			.setProgress(0, 0, true);
		return nBuilder;
	}
	
	/**
	 * Updates the given Notification.Builder object with the supplied parameters. This will update the Notification to use <i>InboxStyle</i>
	 * <br /><br /><i>Note: This will also update the time to <b>System.currentTimeMillis()</b> Please keep this in mind if not immediately calling {@link showNotification()}</i>
	 * @param nBuilder
	 * @param mainTitle
	 * @param nNumber
	 * @param items
	 * @return The Notification.Builder object
	 */
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
			.setWhen(System.currentTimeMillis())
			.setDeleteIntent(getDeleteIntent())
			.setStyle(new Notification.InboxStyle()
				.addLine(items[0])
				.addLine(items[1])
				.setSummaryText(summaryText));		
		
		return nBuilder;
	}
	
	/**
	 * Updates the Notification.Builder with the progress value. This value should be a percent and < 100
	 * 
	 * @param nBuilder
	 * @param progress
	 * @return The Notification.Builder object
	 */
	public Notification.Builder updateProgressNotification(Notification.Builder nBuilder, int progress){
		Log.d(TAG, "Updating progress notification with Id");
		nBuilder.setProgress(100, progress, false);
		
		return nBuilder;
	}
	
	/**
	 * Updates a progress notification to remove the progress bar and allow the notification to be removed by the user.
	 * @param nBuilder
	 * @return
	 */
	public Notification.Builder updateFinishProgressNotification(Notification.Builder nBuilder){
		nBuilder
			.setProgress(0, 0, false)
			.setAutoCancel(true)
			.setOngoing(false);
		return nBuilder;
	}
	
	/**
	 * Cancels the notification with the supplied Id
	 * @param nId
	 */
	public void cancelNotification(int nId){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(nId);
	}
	
	/**
	 * Builds the given Notification.Builder object and then shows it to the user. It is assigned the given Id at build time.
	 * <br /><br /><I>Note: If this is called to show a notification that has been updated, then the time on the notification may be incorrect if not shown directly after being updated.
	 * See {@link updateNotification()} for more info</I>
	 * @param nId
	 * @param nNotification
	 * @return The nId given
	 */
	public int showNotification(int nId, Notification.Builder nNotification){
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(nId, nNotification.build());
		
		return nId;		
	}
	
	protected PendingIntent getDeleteIntent(){
		Log.d(TAG, "Creating PendingIntent for notificaiton");
		Intent intent = new Intent(context, NotificationEventManager.class);
		intent.setAction("cancel_notification");
		
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
	
	

}
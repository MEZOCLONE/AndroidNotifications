package com.matt.remotenotifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DisplayNotification extends Activity {
	
	public void onCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("DISP", "HELLO");
		Context context = DisplayNotification.this.getApplicationContext();
		
		// ---get the notification ID for the notification;
		// passed in by the MainActivity---
		int notifID = getIntent().getExtras().getInt("NotifID");

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notif = new Notification();
		notif.icon = R.drawable.ic_launcher;
		notif.tickerText = "Incoming Remote Notification";
		notif.when = System.currentTimeMillis();

		CharSequence from = getIntent().getExtras().getString("source");
		String incomingMessage = getIntent().getExtras().getString("message");
		CharSequence message = incomingMessage;

		// ---PendingIntent to launch activity if the user selects
		// the notification---
		Intent i = new Intent("com.matt.remotenotifier.MainFragmentActivity");
		i.putExtra("NotifID", notifID);

		PendingIntent onClickIntent = PendingIntent.getActivity(context, 0, i, 0);

		notif.setLatestEventInfo(context, from, message, onClickIntent);

		// Set some settings... we all love settings...
		notif.vibrate = new long[] { 100, 100, 100, 100 };
		notif.flags = Notification.FLAG_SHOW_LIGHTS
				| Notification.FLAG_ONLY_ALERT_ONCE
				| Notification.FLAG_AUTO_CANCEL;

		nm.notify(notifID, notif);
		// ---destroy the activity---
		finish();
	}
}
package com.matt.remotenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class AppPreferences {
	private static String TAG = "RemoteNotifier.appPrefsClass";
	
	private static final String APP_SHARED_PREFS = "com.matt.remotenotifier_preferences"; //.xml
	private SharedPreferences appSharedPreffs;
	private Editor prefsEditior;
	
	public AppPreferences(Context context){
		this.appSharedPreffs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
		this.prefsEditior = appSharedPreffs.edit();
	}
	
	/* Save function 
	 * Lets not commit changes after each write, make sure commitChanges must be called explicitly 
	 * just in case we change our mind
	*/
	private void commitChanges() {
		try{
			prefsEditior.commit();
		}catch(Exception e){
			Log.d(TAG, e.getLocalizedMessage());
		}
	}
		
	// setters	
	protected void savePrevValue(String value){
		prefsEditior.putString("prev_value", value);
		commitChanges();
	}
	
	public void setKey(String value){
		prefsEditior.putString("key", value);
		commitChanges();
	}
	
	public void setSecret(String value){
		prefsEditior.putString("secret", value);
		commitChanges();
	}
	
	public void setLastUpdateDate(String value){
		prefsEditior.putString("last_update_date", value);
		commitChanges();
	}
	
	public void setNewItem(String value){
		prefsEditior.putString("new_item", value);
		commitChanges();
	}
	
	public void setHeartbeatShow(Boolean value){
		prefsEditior.putBoolean("heartbeat_show", value);
		commitChanges();
	}
	
	// Gets
	public String getPrevValue(){
		return appSharedPreffs.getString("prev_value", "");
	}
	
	public String getKey(){
		return appSharedPreffs.getString("key", null);
	}
	
	public String getSecret(){
		return appSharedPreffs.getString("secret", null);
	}
	
	public String getLastUpdateDate(){
		return appSharedPreffs.getString("last_update_date", "");
	}
	
	public String getNewItem(){
		return appSharedPreffs.getString("new_item", "Hello");
	}
	
	public Boolean getHeartbeatShow(){
		return appSharedPreffs.getBoolean("heartbeat_show", false);
	}
	
	
	
	// Counters
	

}

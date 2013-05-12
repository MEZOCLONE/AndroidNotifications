package com.matt.remotenotifier.device;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.matt.remotenotifier.db.BaseDao;
import com.matt.remotenotifier.db.DatabaseHelper;
import com.matt.remotenotifier.db.DeviceTable;

class DeviceDao extends BaseDao {
	
	private String[] allColumns = { DeviceTable.COLUMN_ID, DeviceTable.COLUMN_DEVICE_NAME, 
			DeviceTable.COLUMN_DEVICE_TYPE, DeviceTable.COLUMN_HAS_HEARTBEAT, DeviceTable.COLUMN_LAST_HEARTBEAT_TIME };
	
	public DeviceDao(Context context) {
		dbHelper = new DatabaseHelper(context);
		open();
	}
	
	protected DeviceHolder createDevice(String deviceName, DeviceType deviceType){
		ContentValues values = new ContentValues();
		values.put(DeviceTable.COLUMN_DEVICE_NAME, deviceName);
		values.put(DeviceTable.COLUMN_DEVICE_TYPE, deviceType.toString());
		values.put(DeviceTable.COLUMN_HAS_HEARTBEAT, 0);
		values.put(DeviceTable.COLUMN_LAST_HEARTBEAT_TIME, 0);
		
		long insertId = database.insert(DeviceTable.TABLE_NAME, null, values);
		Cursor cursor = database.query(DeviceTable.TABLE_NAME, allColumns, DeviceTable.COLUMN_ID + " = " + insertId, null, null, null, null);
		
		cursor.moveToFirst();
		DeviceHolder device = cursorToDevice(cursor);
		device.setId(insertId);
		
		return device;
	}
	
	protected DeviceHolder createDevice(DeviceHolder device){
		return createDevice(device.getDeviceName(), device.getDeviceType());		
	}
	
	protected void destroyDevice(DeviceHolder device){
		destroyDevice(device.getId());
	}
	
	protected void destroyDevice(Long id){
		database.delete(DeviceTable.TABLE_NAME, DeviceTable.COLUMN_ID + " = " + id, null);
	}
	
	@SuppressWarnings("unused")
	private void destroyAllDevices(){
		database.delete(DeviceTable.TABLE_NAME, "1", null);
	}
	
	protected void updateHeartbeatTime(DeviceHolder device){
		ContentValues values = new ContentValues();
		values.put(DeviceTable. COLUMN_LAST_HEARTBEAT_TIME, device.getLastHeatbeatTime());
		values.put(DeviceTable.COLUMN_HAS_HEARTBEAT, 1);
		
		database.update(DeviceTable.TABLE_NAME, values, DeviceTable.COLUMN_ID + " = " + device.getId(), null);
	}
	
	protected ArrayList<DeviceHolder> getAllDevices() {
		ArrayList<DeviceHolder> deviceList = new ArrayList<DeviceHolder>();

		Cursor cursor = database.query(DeviceTable.TABLE_NAME, allColumns, null, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			DeviceHolder deviceHolder = cursorToDevice(cursor);
			deviceList.add(0, deviceHolder);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return deviceList;
	}

	private DeviceHolder cursorToDevice(Cursor cursor) {
		DeviceHolder device = new DeviceHolder(cursor.getString(1), DeviceType.valueOf(cursor.getString(2)));
		device.setId(cursor.getLong(0));
		return device;
	}


}

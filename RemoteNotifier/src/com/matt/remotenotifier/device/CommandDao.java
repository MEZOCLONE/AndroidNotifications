package com.matt.remotenotifier.device;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.matt.remotenotifier.db.BaseDao;
import com.matt.remotenotifier.db.CommandTable;
import com.matt.remotenotifier.db.DatabaseHelper;

class CommandDao extends BaseDao {
	private String[] allColumns = { CommandTable.COLUMN_ID, CommandTable.COLUMN_DEVICE_ID, CommandTable.COLUMN_COMMAND_NAME, CommandTable.COLUMN_COMMAND_STRING };
		
	public CommandDao(Context context) {
		dbHelper = new DatabaseHelper(context);
		open();
	}
	
	protected CommandHolder createCommand(DeviceHolder device, String commandName, String commandString){
		ContentValues values = new ContentValues();
		values.put(CommandTable.COLUMN_DEVICE_ID, device.getId());
		values.put(CommandTable.COLUMN_COMMAND_NAME, commandName);
		values.put(CommandTable.COLUMN_COMMAND_STRING, commandString);
		
		long insertId = database.insert(CommandTable.TABLE_NAME, null, values);
		Cursor cursor = database.query(CommandTable.TABLE_NAME, allColumns, CommandTable.COLUMN_ID + " = " + insertId, null, null, null, null);
		
		cursor.moveToFirst();
		CommandHolder command = cursorToCommand(cursor);
		command.setId(insertId);
					
		return command;
	}
	
	protected void destroyCommand(CommandHolder command){
		destroyCommand(command.getId());
	}
	
	protected void destroyCommand(Long id){
		database.delete(CommandTable.TABLE_NAME, CommandTable.COLUMN_ID + " = " + id, null);
	}

	protected ArrayList<CommandHolder> getAllCommands(DeviceHolder device) {
		ArrayList<CommandHolder> commandList = new ArrayList<CommandHolder>();
		
		Long deviceId = device.getId();
		Cursor cursor = database.query(CommandTable.TABLE_NAME, allColumns, CommandTable.COLUMN_DEVICE_ID + " = " + deviceId, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			CommandHolder commandHolder = cursorToCommand(cursor);
			commandList.add(0, commandHolder);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return commandList;
	}

	private CommandHolder cursorToCommand(Cursor cursor) {
		CommandHolder command = new CommandHolder(cursor.getString(2), cursor.getString(3));
		command.setId(cursor.getLong(0));
		return command;
	}

}

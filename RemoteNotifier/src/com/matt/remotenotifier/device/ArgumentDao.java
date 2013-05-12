package com.matt.remotenotifier.device;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.matt.remotenotifier.db.ArgumentTable;
import com.matt.remotenotifier.db.BaseDao;
import com.matt.remotenotifier.db.CommandTable;
import com.matt.remotenotifier.db.DatabaseHelper;

public class ArgumentDao extends BaseDao {
	private String[] allColumns = { ArgumentTable.COLUMN_ID, ArgumentTable.COLUMN_COMMAND_ID, 
			ArgumentTable.COLUMN_NAME, ArgumentTable.COLUMN_TYPE, ArgumentTable.COLUMN_VALUE };
	
	public ArgumentDao(Context context) {
		dbHelper = new DatabaseHelper(context);
		open();
	}
	
	protected ArgumentHolder createArgument(CommandHolder command, String commandName, String commandString){
		ContentValues values = new ContentValues();
		values.put(ArgumentTable.COLUMN_COMMAND_ID, command.getId());
		values.put(ArgumentTable.COLUMN_NAME, commandName);
		values.put(ArgumentTable.COLUMN_TYPE, commandString);
		values.put(ArgumentTable.COLUMN_VALUE, commandString);
		
		long insertId = database.insert(ArgumentTable.TABLE_NAME, null, values);
		Cursor cursor = database.query(ArgumentTable.TABLE_NAME, allColumns, ArgumentTable.COLUMN_ID + " = " + insertId, null, null, null, null);
		
		cursor.moveToFirst();
		ArgumentHolder argument = cursorToArgument(cursor);
		argument.setId(insertId);
					
		return argument;
	}
	
	protected void setValue(ArgumentHolder argument, String value){
		ContentValues values = new ContentValues();
		values.put(ArgumentTable.COLUMN_VALUE, value);
		
		database.update(ArgumentTable.TABLE_NAME, values, ArgumentTable.COLUMN_ID + " = " + argument.getId(), null);
	}
	
	protected void destroyArgument(ArgumentHolder argument){
		destroyArgument(argument.getId());
	}
	
	protected void destroyArgument(Long id){
		database.delete(CommandTable.TABLE_NAME, CommandTable.COLUMN_ID + " = " + id, null);
	}

	protected ArrayList<ArgumentHolder> getAllArguments(CommandHolder command) {
		ArrayList<ArgumentHolder> argumentList = new ArrayList<ArgumentHolder>();
		
		Long commandId = command.getId();
		Cursor cursor = database.query(CommandTable.TABLE_NAME, allColumns, CommandTable.COLUMN_DEVICE_ID + " = " + commandId, null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ArgumentHolder argumentHolder = cursorToArgument(cursor);
			argumentList.add(0, argumentHolder);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return argumentList;
	}

	private ArgumentHolder cursorToArgument(Cursor cursor) {
		ArgumentHolder argument = new ArgumentHolder(cursor.getString(2), cursor.getString(3));
		argument.setId(cursor.getLong(0));
		if(cursor.getString(4) != null){
			argument.setArgValue(cursor.getString(4));
		}
		return argument;
	}
}

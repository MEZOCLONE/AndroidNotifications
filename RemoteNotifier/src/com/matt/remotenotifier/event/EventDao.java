package com.matt.remotenotifier.event;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.matt.remotenotifier.db.BaseDao;
import com.matt.remotenotifier.db.DatabaseHelper;
import com.matt.remotenotifier.db.EventTable;

class EventDao extends BaseDao {

	private String[] allColumns = { EventTable.COLUMN_ID,
			EventTable.COLUMN_MAIN_TEXT, EventTable.COLUMN_SUB_TEXT,
			EventTable.COLUMN_ICON, EventTable.COLUMN_ALPHA,
			EventTable.COLUMN_TIME };

	public EventDao(Context context) {
		dbHelper = new DatabaseHelper(context);
		open();
	}

	public EventHolder createEvent(String mainText, String subText, int iconId, int rowAlpha, long time) {
		ContentValues values = new ContentValues();
		values.put(EventTable.COLUMN_MAIN_TEXT, mainText);
		values.put(EventTable.COLUMN_SUB_TEXT, subText);
		values.put(EventTable.COLUMN_ICON, iconId);
		values.put(EventTable.COLUMN_ALPHA, rowAlpha);
		values.put(EventTable.COLUMN_TIME, time);

		long insertId = database.insert(EventTable.TABLE_NAME, null, values);
		Cursor cursor = database
				.query(EventTable.TABLE_NAME, allColumns, EventTable.COLUMN_ID
						+ " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		EventHolder newEvent = cursorToEvent(cursor);
		cursor.close();
		return newEvent;
	}

	public void deleteEvent(EventHolder event) {
		deleteEvent(event.getId());
	}

	public void deleteEvent(int eventId) {
		database.delete(EventTable.TABLE_NAME, EventTable.COLUMN_ID + " = " + eventId, null);
	}

	/**
	 * Allows deletion of ALL the events. Use with care
	 */
	public void deleteAllEvents() {
		database.delete(EventTable.TABLE_NAME, "1", null);
	}

	public ArrayList<EventHolder> getAllEvents() {
		ArrayList<EventHolder> eventList = new ArrayList<EventHolder>();

		Cursor cursor = database.query(EventTable.TABLE_NAME, allColumns, null,
				null, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			EventHolder eventHolder = cursorToEvent(cursor);
			eventList.add(0, eventHolder);
			cursor.moveToNext();
		}

		// Make sure to close the cursor
		cursor.close();
		return eventList;
	}

	private EventHolder cursorToEvent(Cursor cursor) {
		EventHolder event = new EventHolder(cursor.getString(1),
				cursor.getString(2), cursor.getInt(3), cursor.getInt(4),
				cursor.getLong(5));
		event.setId(cursor.getInt(0));
		return event;
	}

}

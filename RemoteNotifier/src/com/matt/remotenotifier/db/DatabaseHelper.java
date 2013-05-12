package com.matt.remotenotifier.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Generic implementation of the {@link SQLiteOpenHelper}. This should be used to call each of the table classes
 * @author mattm
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	
	private static final String TAG = DatabaseHelper.class.getSimpleName();
	private static final String DATABASE_NAME = "remotenotifier.db";
	private static final int DATABASE_VERSION = 6;


	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		EventTable.onCreate(db);
		CommandTable.onCreate(db);
		JobTable.onCreate(db);
		DeviceTable.onCreate(db);
		ArgumentTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		EventTable.onUpgrade(db, oldVersion, newVersion);
		CommandTable.onUpgrade(db, oldVersion, newVersion);
		JobTable.onUpgrade(db, oldVersion, newVersion);
		DeviceTable.onUpgrade(db, oldVersion, newVersion);
		ArgumentTable.onUpgrade(db, oldVersion, newVersion);
	}

}

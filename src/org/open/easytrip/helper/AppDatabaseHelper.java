package org.open.easytrip.helper;

import org.open.easytrip.bo.BOFactory;
import org.open.easytrip.dao.DAOFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class AppDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "radaralert.db";
	@org.open.easytrip.annotation.CheckLocationBean //Roll up version
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	private static final int DATABASE_VERSION = 5;
	
	private static AppDatabaseHelper mInstance;
	public static AppDatabaseHelper getInstance(Context _context) {
		if (mInstance == null) {
			mInstance = new AppDatabaseHelper(_context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		return mInstance;
	}

	public AppDatabaseHelper(Context _context, String name,
			CursorFactory factory, int version) {
		super(_context, name, factory, version);
		DAOFactory.init(_context); //Possibly not initialized yet
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		BOFactory.getInstance().getDatabaseStructureBO().createDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		BOFactory.getInstance().getDatabaseStructureBO().upgradeDatabase(oldVersion, newVersion);
	}

}

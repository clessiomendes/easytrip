package org.open.easytrip.dao;

import android.database.sqlite.SQLiteDatabase;

public class SQLiteDAO extends AppDAO {
	protected SQLiteDatabase db;
	
	public void setDatabase(SQLiteDatabase _db) {
		this.db = _db;
		//Deprecated only in API level 16
		db.setLockingEnabled(false);
	}

}

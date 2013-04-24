package org.open.easytrip.bo;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseStructureBO extends AppBO {

	public void createDatabase(SQLiteDatabase db) {
		daos.getDatabaseStructureDAO(db).createDatabase();
	}

	@org.open.easytrip.annotation.CheckLocationBean //Call upgrade method to add the new field
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	public void upgradeDatabase(int oldVersion, int newVersion, SQLiteDatabase db) {
		switch (oldVersion+1) {
		//Never break. Keep upgrading...
		case 4:
			daos.getDatabaseStructureDAO(db).upgradeTo4();
		case 5:
			daos.getDatabaseStructureDAO(db).upgradeTo5();
		}
	}

	
}

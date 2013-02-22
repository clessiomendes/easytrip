package org.open.easytrip.dao;

import java.util.Date;

import org.open.easytrip.AppConstants;
import org.open.easytrip.AppUtils;
import org.open.easytrip.entity.YesOrNoEnum;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * DAO
 * @author clessio
 *
 */
@org.open.easytrip.annotation.CheckLocationBean //Create upgrade method to add your field
(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
public class DatabaseStructureDAO extends SQLiteDAO {

	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	public void createDatabase() {
		Log.i(AppConstants.LOG_TAG, "Cleaning up...");		
		db.execSQL("DROP TABLE IF EXISTS locations;");
		Log.i(AppConstants.LOG_TAG, "Creating structure...");		
		db.execSQL("CREATE TABLE locations (" + LocationDAO.ALL_FIELDS_DEFINITIOS + ");");
		//obs: creation will be stored in unix time format for it to be understandable by sqlite date functions
		db.execSQL("CREATE INDEX idx_locations ON locations (latitude, longitude);");
	}
	
	/**
	 * New field "userDefined" on table locations
	 */
	public void upgradeTo4() {
		db.execSQL("ALTER TABLE locations ADD COLUMN userDefined INT;");
		db.execSQL("update locations set userDefined = "+YesOrNoEnum.NO.intValue);
	}
	
	/**
	 * New field "creation" on table locations
	 */
	public void upgradeTo5() {
		db.execSQL("ALTER TABLE locations ADD COLUMN creation DATETIME;");
		db.execSQL("update locations set creation = "+AppUtils.javaDate2sqlite(new Date()));
	}

}

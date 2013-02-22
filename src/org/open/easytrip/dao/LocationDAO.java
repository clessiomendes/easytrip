package org.open.easytrip.dao;

import org.open.easytrip.AppUtils;
import org.open.easytrip.entity.DirectionTypeEnum;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;
import org.open.easytrip.entity.YesOrNoEnum;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * Locations DAO - NOT THREAD SAFE
 * The DAO is not thread safe to be resource optimized (A global SQLiteDatabase instance is held in AppDAO)
 * @author clessio
 *
 */
public class LocationDAO extends SQLiteDAO {
	
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	/**
	 * Used in "select" sql statements
	 */
	private static final String ALL_FIELDS = " id, latitude, longitude , type , speedLimit , directionType , "+
			"direction, userDefined, creation ";
	
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	/**
	 * Used in "create table" sql statements
	 */
	public static final String ALL_FIELDS_DEFINITIOS = " id, latitude, longitude , type , speedLimit , directionType , "+
				"direction, userDefined, creation ";

	/**
	 * Select all locations within range. Important! Only id, latitude and longitude attributes are populated by now. 
	 * @return Array with the found locations. 
	 */
	public LocationBean[] getLocationsByArea(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
		String[] arguments = new String[] {
				Double.toString( minLatitude ),
				Double.toString( maxLatitude ),
				Double.toString( minLongitude ),
				Double.toString( maxLongitude )
		};
		Cursor resultSet = db.rawQuery("select "+ALL_FIELDS+" from locations where latitude > ? and latitude < ? and longitude > ? and longitude < ?", arguments);
		LocationBean[] result = new LocationBean[resultSet.getCount()];
		try {
			
			int i=0;
//			int field;
			while (resultSet.moveToNext()) {
//				LocationBean location = new LocationBean();
//				location.setId(resultSet.getInt(field=0));
//				location.setLatitude(resultSet.getDouble(++field));
//				location.setLongitude(resultSet.getDouble(++field));
//				location.setType(LocationTypeEnum.valueOf(resultSet.getInt(++field)));
//				location.setDirection(resultSet.getInt(++field));
				result[i++] = populateOneInstance(resultSet); 
			}
			return result;
			
		} finally {
			if (resultSet != null)
				resultSet.close();
		}
	}

	/**
	 * 
	 * @param location An object with the id set
	 * @return A new populated instance. Null if none is found.
	 */
	public LocationBean getLocation(int id) {
		if (id == 0)
			return null;
			
		Cursor resultSet = db.rawQuery("select "+ALL_FIELDS+" from locations where id = ?", new String[]{id+""});

		if (! resultSet.moveToNext())
			return null; //No location with the given id was found
		else
			return populateOneInstance(resultSet);
	}

	/**
	 * Fully populates just one instance of a location bean using a cursor. The cursor must have been positioned, i.e. by calling Cursor.moveToNext(), before it is passed.
	 * @param cursor
	 * @return
	 */
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	private LocationBean populateOneInstance(Cursor cursor) {
		if (cursor.isAfterLast() || cursor.isClosed() || cursor.isBeforeFirst())
			return null; //Cursor not correctly positioned or no rows where found
		
		LocationBean result = new LocationBean();
		int field;
		//The id should never be null
		result.setId(cursor.getInt(field=0));
		
		//Fill in the fields, testing for nullity
		if (! cursor.isNull(++field))
			result.setLatitude(cursor.getDouble(field));
		if (! cursor.isNull(++field))
			result.setLongitude(cursor.getDouble(field));
		if (! cursor.isNull(++field))
			result.setType(LocationTypeEnum.valueOf(cursor.getInt(field)));
		if (! cursor.isNull(++field))
			result.setSpeedLimit(cursor.getInt(field));
		if (! cursor.isNull(++field))
			result.setDirectionType(DirectionTypeEnum.valueOf(cursor.getInt(field)));
		if (! cursor.isNull(++field))
			result.setDirection(cursor.getInt(field));
		if (! cursor.isNull(++field))
			result.setUserDefined(YesOrNoEnum.valueOf(cursor.getInt(field)));
		if (! cursor.isNull(++field)) 
			result.setCreation(AppUtils.sqlite2javaDate(cursor.getString(field)));
		return result;
	}

	public int locationsCount() {
		Cursor resultSet = db.rawQuery("select count(*) from locations", new String[]{});
		if (resultSet.moveToNext())
			return resultSet.getInt(0);
		else
			return 0;
	}

	/**
	 * Select all locations within range. Result locations are filled with all attributes. 
	 * @param max maximum length for the result (further records are ignored) 
	 * @return Array with the found locations. 
	 */
	public LocationBean[] getUserDefinedLocations(int max) {
		String[] arguments = new String[] {
				Integer.toString( /*user defined*/ YesOrNoEnum.YES.intValue ),
				Integer.toString( /*limit*/ max )
		};
		Cursor resultSet = db.rawQuery("SELECT "+ALL_FIELDS+" FROM locations WHERE userDefined = ? ORDER BY creation DESC LIMIT ?", arguments);
		LocationBean[] result = new LocationBean[resultSet.getCount()];
		try {
			int i=0;
			
			//Populates each instance and add them to the result array 
			while (resultSet.moveToNext())
				result[i++] = populateOneInstance(resultSet);
			
			return result;
			
		} finally {
			if (resultSet != null)
				resultSet.close();
		}
	}

	/**
	 * Delete by the id
	 * @param id
	 * @return false if not found
	 */
	public boolean delete(int id) {
		return 1 == db.delete("locations", "id = ?", new String[] {""+id});
	}

	/**
	 * Update by the id
	 * @param locationBean
	 * @return false if not found
	 */
	public boolean update(LocationBean locationBean) {
		ContentValues contents = fillContentsExceptId(locationBean);
		return 1 == db.update("locations", contents, "id = ?", new String[] {""+locationBean.getId()});
	}

	/**
	 * Insert the location into the storage for the first time
	 * @param locationBean
	 * @return the auto generated id
	 */
	public long insert(LocationBean locationBean) {
		ContentValues contents = fillContentsExceptId(locationBean);
		//If no id is passed, SQLite engine automatically uses auto-increment
		return db.insertOrThrow("locations", null, contents);
	}

	/**
	 * Create ContentValues user in insert and update statements.
	 * IMPORTANT! The id is not populated. Insert statements will generate it and update 
	 * statements never are expected to update primary keys.
	 * @param locationBean
	 * @return
	 */
	@org.open.easytrip.annotation.CheckLocationBean
	(longitude="",latitude="",id="", direction = "", directionType = "", speedLimit = "", type = "", userDefined = "", creation = "")
	private ContentValues fillContentsExceptId(LocationBean locationBean) {
		ContentValues contents = new ContentValues();
		contents.clear();
		contents.put("latitude", locationBean.getLatitude());
		contents.put("longitude", locationBean.getLongitude());
		contents.put("type", locationBean.getType().intValue);
		contents.put("speedLimit", locationBean.getSpeedLimit());
		contents.put("directionType", locationBean.getDirectionType().intValue);
		contents.put("direction", locationBean.getDirection());
		contents.put("userDefined", locationBean.getUserDefined().intValue);
		contents.put("creation", AppUtils.javaDate2sqlite(locationBean.getCreation()));
		return contents;
	}

	public void clearNonUserLocations() {
		db.delete("locations", "userDefined = 0"/*userDefined is false*/, new String[] {});
	}

	
}

package org.open.easytrip.bo;

import java.io.BufferedReader;
import java.util.Date;

import org.open.easytrip.AppConstants;
import org.open.easytrip.AppUtils;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.entity.DirectionTypeEnum;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;
import org.open.easytrip.entity.YesOrNoEnum;
import org.open.easytrip.exception.AppRuntimeException;

import android.util.Log;


public class UpdateLocationsBO extends AppBO {
	
	/**
	 * Try to insert the location passed. If id is present, update it. 
	 * Any unsuccessful attempt raises exceptions.
	 * Expect changes in the passed instance when inserting (the id will be filled up).
	 * @param locationToSave When inserting, the new generated id is set back to this same instance. When updating, nothing changes.
	 * @return message indicating success
	 */
	public String saveLocation(LocationBean locationToSave) {
		if (locationToSave.getId() != null && locationToSave.getId() > 0) {
			//updates
			if (! daos.getLocationDAO().update(locationToSave))
				throw new AppRuntimeException("Error updating location. "+locationToSave.getId()+" Check SQLite logs.");
				
		} else {
			//inserts
			long id = daos.getLocationDAO().insert(locationToSave);
			if (id == -1/*ErrorFlag*/)
				throw new AppRuntimeException("Error inserting location. Check SQLite logs.");
			//Stores the new generated id back in the passed bean 
			locationToSave.setId((int)id);
		}
		return "Alert saved successfully ("+locationToSave.getId()+")";
	}

	/**
	 * Plain delete by id 
	 */
	public String delete(int id) {
		if (daos.getLocationDAO().delete(id))
			return "Location "+id+" successfully deleted";
		else
			throw new AppRuntimeException("Error deleting location "+id+". Check SQLite logs.");
	}
}

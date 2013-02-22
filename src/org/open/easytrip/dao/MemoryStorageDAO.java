package org.open.easytrip.dao;

import org.open.easytrip.entity.LocationBean;

/**
 * A simple DAO that stores important application data in memory only (not persisted through sessions). 
 */
public class MemoryStorageDAO extends AppDAO {
	
	private LocationBean lastLocationAlarmed = null;
	
	public LocationBean getLastLocationAlarmed() {
		return lastLocationAlarmed;
	}

	public void setLastLocationAlarmed(LocationBean closest) {
		this.lastLocationAlarmed = closest;
	}
	
	

}

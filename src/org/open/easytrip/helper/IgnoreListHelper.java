package org.open.easytrip.helper;

import android.util.SparseArray;

/**
 * Singleton to keep a map of location IDs/timestamps to be eventually ignored on warnings.
 * The key of the map is the location id and its value is a timestamp (for an extra expiration test)
 */
public class IgnoreListHelper {
//TODO Consider not using a helper/singleton to store the locations ignore list. 
	
	/**
	 * The map where the IDs to be ignored will be stored. 
	 */
	private SparseArray<Long> ignoreLocations = new SparseArray<Long>();
	private static final long IGNORE_LIST_EXPIRE_INTERVAL = 2 * 60 * 1000/*ms*/;
	private static IgnoreListHelper instance;
	
	/**
	 * Can not be explicitly created.
	 */
	private IgnoreListHelper() {
	}
	
	/**
	 * Singleton instance implementation.  
	 */
	public static IgnoreListHelper getInstance() {
		if (instance == null)
			instance = new IgnoreListHelper();
		return instance;
	}

	/**
	 * Register a new location to be ignored, storing its timestamp 
	 */
	public void put(int id) {
		ignoreLocations.put(id, System.currentTimeMillis());
	}

	/**
	 * Test if the location is marked not to be warned.
	 * @return True indicating the location should not be warned. False otherwise.
	 */
	public boolean ignore(int id) {
		final Long expireTime = ignoreLocations.get(id); 
		if (expireTime != null) {
			//Double check if the ignore timestamp has expired, in witch case the location will not be ignored
			final long currentTime = System.currentTimeMillis();
			if (currentTime - expireTime < IGNORE_LIST_EXPIRE_INTERVAL) {
				return true;
			}
		}
		return false;
	}
}

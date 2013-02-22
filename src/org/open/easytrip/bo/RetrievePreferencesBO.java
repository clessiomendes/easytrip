package org.open.easytrip.bo;

import java.util.ArrayList;
import java.util.List;

import org.open.easytrip.R;
import org.open.easytrip.control.AppActivity;
import org.open.easytrip.dao.DAOFactory;
import org.open.easytrip.entity.LocationBean;
import org.open.easytrip.entity.LocationTypeEnum;

import android.preference.PreferenceManager;

/**
 * Only for retrieving. Storing is handled by Android infrastructure.
 * @author clessio
 */
public class RetrievePreferencesBO extends AppBO {

	public LocationBean getLocation(int id) {
		return daos.getLocationDAO().getLocation(id);
	}

	/**
	 * @return GPS interval between successive signals, in seconds
	 */
	public int getGpsInterval() {
		return daos.getSharedPreferencesDAO().getGpsInterval();
	} 

	/**
	 * @return Range, in meters, to search for locations
	 */
	public int getSearchRadius() {
		return daos.getSharedPreferencesDAO().getSearchRadius();
	}

	/**
	 * @return If sound alarm is enabled
	 */
	public boolean isSoundAlarm() {
		return daos.getSharedPreferencesDAO().isSoundAlarm();
	}

	/**
	 * @return If vibration alarm is enabled
	 */
	public boolean isVibrationAlarm() {
		return daos.getSharedPreferencesDAO().isVibrationAlarm();
	}

	/**
	 * @return If playing an intermittent beep to show the GPS is working 
	 */
	public boolean isBeep() {
		return daos.getSharedPreferencesDAO().isBeep();
	}

	/**
	 * @return Should app keep running in background?
	 * TODO Remove: Act like google navigator. Always keep running in background, except if the user explicitly exit. Leave a notification.
	 */
	public boolean isRunInBackgroud() {
		return daos.getSharedPreferencesDAO().isRunInBackgroud();
	}

	/**
	 * @return Which types of alert should be alarmed to the user (speed cameras, red light, etc.)
	 */
	public List<LocationTypeEnum> getWarningTypes() {
		return daos.getSharedPreferencesDAO().getWarningTypes();
	}
	
}

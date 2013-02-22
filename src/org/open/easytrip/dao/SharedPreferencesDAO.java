package org.open.easytrip.dao;

import java.util.ArrayList;
import java.util.List;

import org.open.easytrip.R;
import org.open.easytrip.entity.LocationTypeEnum;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

public class SharedPreferencesDAO extends AppDAO {

	private SharedPreferences sharedPreferences;
	private Resources resources;

	public SharedPreferencesDAO(SharedPreferences sharedPreferences,
			Resources resources) {
		super();
		this.sharedPreferences = sharedPreferences;
		this.resources = resources;
	}

//	public void setSharedPreferences(SharedPreferences sharedPreferences) {
//		this.sharedPreferences = sharedPreferences;
//	}
//
//	public void setResources(Resources resources) {
//		this.resources = resources;
//	}

	public int getGpsInterval() {
		String key = resources.getString(R.string.preferences_gps_interval);
		String def = resources.getString(R.string.gps_interval_default);
		String entry = sharedPreferences.getString(key, def);
		return Integer.parseInt(entry) * 1000/*convert to miliseconds*/;
	} 

	public int getSearchRadius() {
		String key = resources.getString(R.string.preferences_search_radius);
		String def = resources.getString(R.string.search_radius_default);
		return Integer.parseInt(sharedPreferences.getString(key, def));
	}

	public boolean isSoundAlarm() {
		String key = resources.getString(R.string.preferences_sound_alarm);
		return sharedPreferences.getBoolean(key, true);
	}

	public boolean isVibrationAlarm() {
		String key = resources.getString(R.string.preferences_vibration_alarm);
		return sharedPreferences.getBoolean(key, true);
	}

	public boolean isBeep() {
		String key = resources.getString(R.string.preferences_beep);
		return sharedPreferences.getBoolean(key, true);
	}

	public boolean isRunInBackgroud() {
		String key = resources.getString(R.string.preferences_run_in_backgroud);
		return sharedPreferences.getBoolean(key, true);
	}

	public List<LocationTypeEnum> getWarningTypes() {
		String keyPrefix = resources.getString(R.string.preferences_location_types);
		List<LocationTypeEnum> result = new ArrayList<LocationTypeEnum>();
		//Test if each enumeration has a corresponding key in SharedLocations set to true
		for (LocationTypeEnum locationType : LocationTypeEnum.values()) {
			String compoundKey = keyPrefix+"."+locationType.toString();
			boolean defaultValue = true; //Warn by default if the location type is not set in preferences yet
			if (sharedPreferences.getBoolean(compoundKey, defaultValue))
				result.add(locationType);
		}
		return result;
	}

	public long getLastImportFileSize() {
		String key = resources.getString(R.string.shared_preferences_las_import_file_size);
		return sharedPreferences.getLong(key, 0);
	}
	
	public void setLastImportFileSize(long value) {
		String key = resources.getString(R.string.shared_preferences_las_import_file_size);
		final Editor edit = sharedPreferences.edit();
		edit.putLong(key, value);
		edit.commit();
	}

}	

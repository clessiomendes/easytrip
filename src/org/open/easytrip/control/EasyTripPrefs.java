package org.open.easytrip.control;

import org.open.easytrip.R;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class EasyTripPrefs extends PreferenceActivity {
	@Override
	@TargetApi(11)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		MultiSelectListPreference m = new MultiSelectListPreference(this);
		m.setKey("preferences_location_types");
		m.setTitle("Events to be alarmed");
		Object o = null;
		m.setDefaultValue(o);

		CharSequence[] entries = { "Dark grey", "Light grey", "Light red", "Red" };
		CharSequence[] entryValues = { "#4c4c4c", "#b5b5b5", "#ab6a68", "#962622" };
		m.setEntries(entries);
		m.setEntryValues(entryValues);
	}
	
//	@Override
//	protected void onPause() {
//		super.onPause();
//	    Preference locationTypesPref = 
//	    		findPreference("location_types");
//	    if(locationTypesPref.getSharedPreferences().
//	        getBoolean(locationTypesPref.getKey(), false)){
//	        // apply reset, and then set the pref-value back to false 
//	    }
//	}	
}

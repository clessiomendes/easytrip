package org.open.easytrip;

public abstract class AppConstants {
	public static final long EARTH_RADIUS =  6371000/*m*/;
	public static final int GPS_REFRESH_INTERVAL = 1000/*ms*/;
	public static final String LOG_TAG = "EasyTrip";
	public static final boolean START_OVER = false;
	public static final int NEW_LOCATION_MAXIMUM_ELAPSED_TIME = 10 * 1000 /*10s*/;
	public static final char[] showAliveDisplay = {'|','/','-','\\'};
	
}

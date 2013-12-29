package org.open.easytrip;

import java.util.Date;

import org.open.easytrip.entity.GpsMovement;
import org.open.easytrip.exception.AppRuntimeException;

import android.location.Location;

public abstract class AppUtils {
	public static double degrees2meters(double degrees) {
		return (degrees / 360) * 2 * Math.PI * AppConstants.EARTH_RADIUS;
	}
	
	public static double meters2degrees(double meters) {
		return 360 * meters / (2 * Math.PI * AppConstants.EARTH_RADIUS);
	}
	
	/**
	 * To be used in unit tests
	 */
	public static void test() {
		System.out.println("1 degree = "+degrees2meters(1)+" meters");
		System.out.println("111200 meters = "+meters2degrees(111200)+" degrees");
	}

	/**
	 * Parse string as double raising an exception indicating which field failed
	 * @param stringValue
	 * @param fieldName Used to compose error message
	 * @return
	 */
	public static double parseDouble(String stringValue, String fieldName) {
		try {
			return Double.valueOf(stringValue);
		} catch (Exception e) {
			throw new AppRuntimeException("Error parsing "+fieldName+" '"+stringValue+"'", e);
		}
	}
	
	/**
	 * Parse string as integer raising an exception indicating which field failed
	 * @param stringValue
	 * @param fieldName Used to compose error message
	 * @return
	 */
	public static int parseInteger(String stringValue, String fieldName) {
		try {
			return Integer.valueOf(stringValue);
		} catch (Exception e) {
			throw new AppRuntimeException("Error parsing "+fieldName+" '"+stringValue+"'", e);
		}
	}
	
	
	/**
	 * Test proximity by identifying if coordinates are within a <b>square</b> with aproxDistance x 2 side size. 
	 * The test doesn't use radius comparisons (a 23% smaller area) to consume less resources. 
	 * @param currentLatitude
	 * @param currentLongitude
	 * @param compareLatitude
	 * @param compareLongitude
	 * @param aproxDistance in decimal degrees. 1000 meters = 0.01 degrees
	 * @return
	 */
	public static boolean squareProximity(double currentLatitude, double currentLongitude, double compareLatitude, double compareLongitude, double aproxDistance) {
		return currentLatitude > compareLatitude - aproxDistance && currentLatitude < compareLatitude + aproxDistance
				&&
				currentLongitude > compareLongitude - aproxDistance && currentLongitude < compareLongitude + aproxDistance;
	}

	/**
	 * Test proximity by identifying if coordinates are within a <b>circle</b> with squareRoot(distanceSquare) radius. 
	 * This test use exactly a radius comparisons and consume more resources then squareProximity. 
	 * @param distanceSquare distance radius, in decimal degrees. 1000 meters = 0.009 degrees. The square power of the distance must be 
	 * calculated in advance to permit optimization.
	 * @return
	 */
	public static boolean radiusProximity(double currentLatitude, double currentLongitude, double compareLatitude, double compareLongitude, double distanceSquare) {
		return distanceSquare > distanceSquare(currentLatitude, currentLongitude, compareLatitude, compareLongitude);
	}

	private static double distanceSquare(double currentLatitude, double currentLongitude, double compareLatitude, double compareLongitude) {
		return (currentLatitude - compareLatitude) * (currentLatitude - compareLatitude)  +
				(currentLongitude - compareLongitude) * (currentLongitude - compareLongitude);
	}
	
	public static double distance(double currentLatitude, double currentLongitude, double compareLatitude, double compareLongitude) {
		return Math.hypot(currentLongitude - compareLongitude, currentLatitude - compareLatitude);
//		return Math.sqrt(Math.pow(currentLatitude - compareLatitude,2) + Math.pow(currentLongitude - compareLongitude,2));
	}
	
	public static boolean isDevelopmentTime() {
		return true;
	}

	/**
	 * Generates a string SQL expression to convert java date to sqlite unix style  
	 */
	public static String javaDate2sqlite(Date javaDate) {
		return ""+(javaDate.getTime()/1000L);
	}
	
	/**
	 * takes a sqlite unix style representation and convert to java Date  
	 */
	public static Date sqlite2javaDate(String sqliteDate) {
		return new Date(Long.parseLong(sqliteDate)*1000L);
//		return "(strftime('%s', "+fieldName+", 'unixepoch') * 1000) AS "+fieldName;
	}

	/**
	 * The absolute difference between two angles, considering 360 == 0  
	 */
	public static int absDirectionDiffDegrees(int a, int b) {
		return Math.abs(180 - Math.abs(180 - Math.abs(a - b)));
	}
	
	/**
	 * The absolute difference between two angles, considering 2pi == 0  
	 */
	public static double absDirectionDiffRadians(double a, double b) {
		return Math.abs(Math.PI - Math.abs(Math.PI - Math.abs(a - b)));
	}

	/**
	 * Converts speed from m/s to km/h
	 * @param speedMs
	 * @return
	 */
	public static int ms2Kmh(float speedMs) {
		return (int)Math.round(speedMs * 3.6);
	}

	/**
	 * Converts from android.location.Location to org.open.easytrip.GpsMovement
	 */
	public static GpsMovement convert(Location location) {
		GpsMovement result = new GpsMovement();
		result.setBearing(location.getBearing());
		result.setSpeed(location.getSpeed());
		result.setLatitude(location.getLatitude());
		result.setLongitude(location.getLongitude());
		result.setTime(location.getTime());
		return result;
	}
	
	
}

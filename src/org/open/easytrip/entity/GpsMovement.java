package org.open.easytrip.entity;

import org.open.easytrip.AppUtils;

public class GpsMovement {
	private float bearing;
	private float speed;
	private double latitude;
	private double longitude;
	private long time;
	
	public float getBearing() {
		return bearing;
	}
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
	public float getSpeedMs() {
		return speed;
	}
	public float getSpeedKh() {
		return AppUtils.ms2Kmh(speed);
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	

}

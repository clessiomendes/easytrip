package org.open.easytrip.entity;

import org.open.easytrip.R;

public enum LocationTypeEnum {
	
	FIXED_SPEED_CAMERA(1, R.string.location_type_fixed_speed_camera, true),
	RED_LIGHT_CAMERA(3, R.string.location_type_red_light_camera, false),
	TRAFFIC_LIGHT_SPEED_CAMERA(2, R.string.location_type_traffic_light_speed_camera, true),
	RELOCATABLE_SPEED_CAMERA(5, R.string.location_type_relocatable_speed_camera, true),
	HIGHWAY_PATROL_STATION(7, R.string.location_type_highway_patrol_station, true),
	SPEED_BUMP(18, R.string.location_type_speed_bump, false),
	OTHERS(0, R.string.location_type_others, false);

	/**
	 * Integer code present in import files (and stored in the database)
	 */
	public final int intValue;
	/**
	 * Used to match externalized descriptions  
	 */
	public final int resourceId;
	/**
	 * Signals if this kind of alert should consider speed.
	 */
	public final boolean speedControl;
	
	private LocationTypeEnum(int _intValue, int _resourceId, boolean _speedControl) {
		this.intValue = _intValue;
		this.resourceId = _resourceId;
		this.speedControl = _speedControl;
	}
	
	/**
	 * Reverse enum by its intValue.
	 * @return the matched enum or null if none 
	 */
	public static LocationTypeEnum valueOf(int _intValue) {
		for (LocationTypeEnum value : LocationTypeEnum.values())
			if (value.intValue == _intValue)
				return value;
		return null;
//		throw new InvalidEnumCodeException("No corresponding enum code in LocationTypeEnum for int "+_intValue, YesOrNoEnum.class, _intValue);
	}
	
}

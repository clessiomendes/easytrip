package org.open.easytrip.entity;

import org.open.easytrip.R;


public enum DirectionTypeEnum {
	ONE_DIRECTION(1, R.string.direction_type_one_direction),
	BOTH_DIRECTIONS(2, R.string.direction_type_both_directions),
	ALL_DIRECTIONS(0, R.string.direction_type_all_directions);
	
	public final int intValue;
	public final int resourceId;
	
	private DirectionTypeEnum(int _intValue, int _resourceId) {
		this.intValue = _intValue;
		this.resourceId = _resourceId;
	}

	/**
	 * Reverse enum by its intValue
	 */
	public static DirectionTypeEnum valueOf(int _intValue) {
		for (DirectionTypeEnum value : DirectionTypeEnum.values())
			if (value.intValue == _intValue)
				return value;
		return null;
	}
		
}

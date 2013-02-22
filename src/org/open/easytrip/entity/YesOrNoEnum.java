package org.open.easytrip.entity;


public enum YesOrNoEnum {
	NO(0),
	YES(1);
	
	public final int intValue;
	
	private YesOrNoEnum(int _intValue) {
		this.intValue = _intValue;
	}

	/**
	 * Reverse enum by its intValue
	 */
	public static YesOrNoEnum valueOf(int _intValue) {
		for (YesOrNoEnum value : YesOrNoEnum.values())
			if (value.intValue == _intValue)
				return value;
		return null;
//		throw new InvalidEnumCodeException("No corresponding enum code in YesOrNoEnum for int "+_intValue, YesOrNoEnum.class, _intValue);
	}
		
}

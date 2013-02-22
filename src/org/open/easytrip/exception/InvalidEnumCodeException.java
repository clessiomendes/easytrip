package org.open.easytrip.exception;


/**
 * Raised when having issues converting from integer codes to enum types
 */
public class InvalidEnumCodeException extends AppRuntimeException {

	private static final long serialVersionUID = -9014047206935640417L;
	
	private Class enumClass;
	private int invalidValue;

	public InvalidEnumCodeException() {
		super();
	}

	public InvalidEnumCodeException(String detailMessage, Class _enumClass, int _invalidValue) {
		super(detailMessage);
		this.enumClass = _enumClass;
		this.invalidValue = _invalidValue;
	}

	public Class<Enum> getEnumClass() {
		return enumClass;
	}

	public int getInvalidValue() {
		return invalidValue;
	}
	
}

package org.open.easytrip.exception;

/**
 * Base class for runtime exceptions explictly thrown by the application
 * @author clessio
 *
 */
public class AppRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 6137388301147686497L;

	public AppRuntimeException() {
		super();
	}

	public AppRuntimeException(String detailMessage) {
		super(detailMessage);
	}

	public AppRuntimeException(Throwable throwable) {
		super(throwable);
	}

	public AppRuntimeException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}

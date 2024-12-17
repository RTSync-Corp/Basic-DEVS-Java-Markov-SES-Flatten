package com.ms4systems.devs.exception;

public class MethodNotSupportedException extends DEVSRuntimeException {
	private static final long serialVersionUID = 1L;

	public MethodNotSupportedException() {
		super();
	}

	public MethodNotSupportedException(String message) {
		super(message);
	}

	public MethodNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodNotSupportedException(Throwable cause) {
		super(cause);
	}

}

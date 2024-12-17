package com.ms4systems.devs.exception;

public class InvalidCouplingException extends DEVSRuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidCouplingException() {
	}

	public InvalidCouplingException(String message) {
		super(message);
	}

	public InvalidCouplingException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCouplingException(Throwable cause) {
		super(cause);
	}

}

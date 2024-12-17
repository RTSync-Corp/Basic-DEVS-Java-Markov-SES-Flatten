package com.ms4systems.devs.exception;

public class DEVSRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public DEVSRuntimeException() {
    	super();
    }

    public DEVSRuntimeException(String message) {
    	super(message);
    }

    public DEVSRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DEVSRuntimeException(Throwable cause) {
        super(cause);
    }
    
}

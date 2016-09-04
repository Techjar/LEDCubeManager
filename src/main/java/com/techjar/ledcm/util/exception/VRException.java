package com.techjar.ledcm.util.exception;

public class VRException extends RuntimeException {
	private static final long serialVersionUID = -1791966618257014615L;

	public VRException() {
		super();
	}

	public VRException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public VRException(String message, Throwable cause) {
		super(message, cause);
	}

	public VRException(String message) {
		super(message);
	}

	public VRException(Throwable cause) {
		super(cause);
	}
}

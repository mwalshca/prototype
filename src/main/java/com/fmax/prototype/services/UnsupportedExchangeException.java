package com.fmax.prototype.services;

public class UnsupportedExchangeException extends Exception {
	private static final long serialVersionUID = 6055033004945719644L;

	public UnsupportedExchangeException() {
	}

	public UnsupportedExchangeException(String message) {
		super(message);
	}

	public UnsupportedExchangeException(Throwable cause) {
		super(cause);
	}

	public UnsupportedExchangeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedExchangeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

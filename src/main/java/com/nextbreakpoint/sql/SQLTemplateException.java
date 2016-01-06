package com.nextbreakpoint.sql;

public class SQLTemplateException extends Exception {
	private static final long serialVersionUID = 1L;

	public SQLTemplateException(String message) {
		super(message);
	}

	public SQLTemplateException(String message, Throwable cause) {
		super(message, cause);
	}
}

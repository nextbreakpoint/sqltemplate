/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.Objects;

/**
 * Exception thrown to report errors.
 * 
 * @author Andrea
 *
 */
public class SQLTemplateException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new instance with given message.
	 * @param message the message
	 */
	public SQLTemplateException(String message) {
		super(message);
		Objects.requireNonNull(message);
	}

	/**
	 * Creates new instance with given message and cause.
	 * @param message the message
	 * @param cause the cause
	 */
	public SQLTemplateException(String message, Throwable cause) {
		super(message, cause);
		Objects.requireNonNull(message);
		Objects.requireNonNull(cause);
	}
}

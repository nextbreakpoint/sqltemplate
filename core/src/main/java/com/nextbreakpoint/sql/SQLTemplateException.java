/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.Objects;

public class SQLTemplateException extends Exception {
	private static final long serialVersionUID = 1L;

	public SQLTemplateException(String message) {
		super(message);
		Objects.requireNonNull(message);
	}

	public SQLTemplateException(String message, Throwable cause) {
		super(message, cause);
		Objects.requireNonNull(message);
		Objects.requireNonNull(cause);
	}
}

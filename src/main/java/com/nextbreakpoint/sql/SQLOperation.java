/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

/**
 * Represents a sequence of JDBC operations.
 * 
 * @author Andrea
 *
 */
@FunctionalInterface
interface SQLOperation {
	/**
	 * Invokes operations on given SQLDriver and returns the result as Try instance.
	 * @param driver the SQLDriver
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> apply(SQLDriver driver);

	/**
	 * Appends another operation.
	 * @param other an operation
	 * @return new operation
	 */
	public default SQLOperation andThen(SQLOperation other) {
		return driver -> this.apply(driver).flatMap(otherDriver -> other.apply(otherDriver));
	}
}

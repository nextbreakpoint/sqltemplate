/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

/**
 * Represents a JDBC operations.
 * 
 * @author Andrea
 *
 */
@FunctionalInterface
public interface SQLFunction {
	/**
	 * Invoke operations on given SQLDriver and returns the result as Try instance.
	 * @param sql the SQLDriver
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> apply(SQLDriver sql);

	/**
	 * Concatenate function with given function.
	 * @param other the function
	 * @return the new function
	 */
	public default SQLFunction andThen(SQLFunction other) {
		return driver -> this.apply(driver).flatMap(driver2 -> other.apply(driver2));
	}
}
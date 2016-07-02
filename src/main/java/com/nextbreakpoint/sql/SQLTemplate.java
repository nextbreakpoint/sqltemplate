/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

/**
 * SQLTemplate implements a functional API for executing SQL statements.
 * 
 * @author Andrea
 *
 */
public class SQLTemplate {
	private final SQLOperation operation;

	SQLTemplate(SQLOperation operation) {
		Objects.requireNonNull(operation);
		this.operation = operation;
	}

	/**
	 * Attempts to apply the operations and returns the result as Try instance.
	 * @param connection a JDBC connection
	 * @return the result
	 */
	public Try<List<Object[]>, SQLTemplateException> apply(Connection connection) {
		return operation.apply(SQLTemplateDriver.create(connection)).map(driver -> driver.values());
	}

	/**
	 * Creates a builder create given connection.
	 * @return the builder
	 */
	public static SQLTemplateBuilder builder() {
		return SQLTemplateBuilder.create();
	}
}

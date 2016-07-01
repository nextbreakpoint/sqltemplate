/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.util.Objects;

import com.nextbreakpoint.Try;
import java.util.List;

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
	 * Attempts to run the operations and returns the result as Try instance.
	 * @param connection a JDBC connection
	 * @return the result
	 */
	public Try<List<Object[]>, SQLTemplateException> run(Connection connection) {
		return operation.apply(SQLDriver.create(connection)).map(driver -> driver.values());
	}

	/**
	 * Creates a builder create given connection.
	 * @return the builder
	 */
	public static SQLBuilder builder() {
		return SQLBuilder.create();
	}
}

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
	private final Connection connection;

	SQLTemplate(Connection connection, SQLOperation operation) {
		Objects.requireNonNull(operation);
		Objects.requireNonNull(connection);
		this.connection = connection;
		this.operation = operation;
	}

	/**
	 * Attempts to executeUpdate the operations and returns the result as Try instance.
	 * @return the result
	 */
	public Try<List<Object[]>, SQLTemplateException> run() {
		return operation.apply(SQLDriver.create(connection)).map(driver -> driver.values());
	}

	/**
	 * Creates a builder with given connection.
	 * @param connection the connection
	 * @return the builder
	 */
	public static SQLBuilder builder(Connection connection) {
		return SQLBuilder.with(connection);
	}
}

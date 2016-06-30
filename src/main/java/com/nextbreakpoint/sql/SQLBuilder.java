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

/**
 * Provides a builder for creating a SQLTemplate.
 * 
 * @author Andrea
 *
 */
public class SQLBuilder {
	private final Connection connection;
	private final SQLOperation operation;

	private SQLBuilder(Connection connection, SQLOperation operation) {
		Objects.requireNonNull(connection);
		Objects.requireNonNull(operation);
		this.connection = connection;
		this.operation = operation;
	}

	private static SQLBuilder create(Connection connection, SQLOperation operation) {
		return new SQLBuilder(connection, operation);
	}

	/**
	 * Creates a new empty command.
	 * @param connection the connection
	 * @return new command
	 */
	public static SQLBuilder with(Connection connection) {
		return create(connection, sql -> Try.success(SQLDriver.defaultMapper(), sql));
	}

	/**
	 * Appends autoCommit command.
	 * @return new command
	 */
	public SQLBuilder autoCommit() {
		return create(connection, operation.andThen(driver -> driver.autoCommit()));
	}

	/**
	 * Appends noAutoCommit command.
	 * @return new command
	 */
	public SQLBuilder noAutoCommit() {
		return create(connection, operation.andThen(driver -> driver.noAutoCommit()));
	}
	
	/**
	 * Appends commit command.
	 * @return new command
	 */
	public SQLBuilder commit() {
		return create(connection, operation.andThen(driver -> driver.commit()));
	}
	
	/**
	 * Appends rollback command.
	 * @return new command
	 */
	public SQLBuilder rollback() {
		return create(connection, operation.andThen(driver -> driver.rollback()));
	}

	/**
	 * Appends prepareStatement command.
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public SQLBuilder prepareStatement(String sqlStmt) {
		return create(connection, operation.andThen(driver -> driver.prepareStatement(sqlStmt)));
	}
	
	/**
	 * Appends executeUpdate with arguments command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder executeUpdate(Object[] params) {
		return create(connection, operation.andThen(driver -> driver.executeUpdate(params)));
	}

	/**
	 * Appends executeQuery with arguments command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder executeQuery(Object[] params) {
		return create(connection, operation.andThen(driver -> driver.executeQuery(params)));
	}

	/**
	 * Appends executeUpdate command.
	 * @return new command
	 */
	public SQLBuilder executeUpdate() {
		return create(connection, operation.andThen(driver -> driver.executeUpdate()));
	}

	/**
	 * Appends executeQuery command.
	 * @return new command
	 */
	public SQLBuilder executeQuery() {
		return create(connection, operation.andThen(driver -> driver.executeQuery()));
	}

	/**
	 * Builds the template.
	 * @return new template
	 */
	public SQLTemplate build() {
		return new SQLTemplate(connection, operation);
	}
}

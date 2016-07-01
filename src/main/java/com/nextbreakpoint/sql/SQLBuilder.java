/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.Objects;

import com.nextbreakpoint.Try;

/**
 * Provides a builder for creating a SQLTemplate.
 * 
 * @author Andrea
 *
 */
public class SQLBuilder {
	private final SQLOperation operation;

	private SQLBuilder(SQLOperation operation) {
		Objects.requireNonNull(operation);
		this.operation = operation;
	}

	private static SQLBuilder create(SQLOperation operation) {
		return new SQLBuilder(operation);
	}

	/**
	 * Creates a new empty command.
	 * @return new command
	 */
	public static SQLBuilder create() {
		return create(sql -> Try.success(SQLDriver.defaultMapper(), sql));
	}

	/**
	 * Appends autoCommit command.
	 * @return new command
	 */
	public SQLBuilder autoCommit() {
		return create(operation.andThen(driver -> driver.autoCommit()));
	}

	/**
	 * Appends noAutoCommit command.
	 * @return new command
	 */
	public SQLBuilder noAutoCommit() {
		return create(operation.andThen(driver -> driver.noAutoCommit()));
	}
	
	/**
	 * Appends commit command.
	 * @return new command
	 */
	public SQLBuilder commit() {
		return create(operation.andThen(driver -> driver.commit()));
	}
	
	/**
	 * Appends rollback command.
	 * @return new command
	 */
	public SQLBuilder rollback() {
		return create(operation.andThen(driver -> driver.rollback()));
	}

	/**
	 * Appends prepareStatement command.
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public SQLBuilder prepareStatement(String sqlStmt) {
		return create(operation.andThen(driver -> driver.prepareStatement(sqlStmt)));
	}
	
	/**
	 * Appends executeUpdate create arguments command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder executeUpdate(Object[] params) {
		return create(operation.andThen(driver -> driver.executeUpdate(params)));
	}

	/**
	 * Appends executeQuery create arguments command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder executeQuery(Object[] params) {
		return create(operation.andThen(driver -> driver.executeQuery(params)));
	}

	/**
	 * Appends executeUpdate command.
	 * @return new command
	 */
	public SQLBuilder executeUpdate() {
		return create(operation.andThen(driver -> driver.executeUpdate()));
	}

	/**
	 * Appends executeQuery command.
	 * @return new command
	 */
	public SQLBuilder executeQuery() {
		return create(operation.andThen(driver -> driver.executeQuery()));
	}

	/**
	 * Builds the template.
	 * @return new template
	 */
	public SQLTemplate build() {
		return new SQLTemplate(operation);
	}
}

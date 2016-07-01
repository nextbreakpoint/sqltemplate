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
	 * Creates a empty command.
	 * @return new command
	 */
	public static SQLBuilder create() {
		return create(sql -> Try.success(SQLDriver.defaultMapper(), sql));
	}

	/**
	 * Appends set auto commit true.
	 * @return new command
	 */
	public SQLBuilder autoCommit() {
		return create(operation.andThen(driver -> driver.autoCommit()));
	}

	/**
	 * Appends set auto commit false.
	 * @return new command
	 */
	public SQLBuilder noAutoCommit() {
		return create(operation.andThen(driver -> driver.noAutoCommit()));
	}
	
	/**
	 * Appends commit.
	 * @return new command
	 */
	public SQLBuilder commit() {
		return create(operation.andThen(driver -> driver.commit()));
	}
	
	/**
	 * Appends rollback.
	 * @return new command
	 */
	public SQLBuilder rollback() {
		return create(operation.andThen(driver -> driver.rollback()));
	}

	/**
	 * Appends prepare statement.
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public SQLBuilder statement(String sqlStmt) {
		return create(operation.andThen(driver -> driver.prepareStatement(sqlStmt)));
	}
	
	/**
	 * Appends update with arguments.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder update(Object[] params) {
		return create(operation.andThen(driver -> driver.executeUpdate(params)));
	}

	/**
	 * Appends query with arguments.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLBuilder query(Object[] params) {
		return create(operation.andThen(driver -> driver.executeQuery(params)));
	}

	/**
	 * Appends execute update.
	 * @return new command
	 */
	public SQLBuilder update() {
		return create(operation.andThen(driver -> driver.executeUpdate()));
	}

	/**
	 * Appends execute query.
	 * @return new command
	 */
	public SQLBuilder query() {
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

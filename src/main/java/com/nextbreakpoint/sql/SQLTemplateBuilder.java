/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.util.Objects;

/**
 * Provides a builder for creating a SQLTemplate.
 * 
 * @author Andrea Medeghini
 *
 */
public class SQLTemplateBuilder {
	private final SQLOperation operation;

	private SQLTemplateBuilder(SQLOperation operation) {
		Objects.requireNonNull(operation);
		this.operation = operation;
	}

	private static SQLTemplateBuilder create(SQLOperation operation) {
		return new SQLTemplateBuilder(operation);
	}

	/**
	 * Creates a empty command.
	 * @return new command
	 */
	public static SQLTemplateBuilder create() {
		return create(sql -> Try.success(SQLTemplateDriver.defaultMapper(), sql));
	}

	/**
	 * Appends set auto commit true.
	 * @return new command
	 */
	public SQLTemplateBuilder autoCommit() {
		return create(operation.andThen(driver -> driver.autoCommit()));
	}

	/**
	 * Appends set auto commit false.
	 * @return new command
	 */
	public SQLTemplateBuilder noAutoCommit() {
		return create(operation.andThen(driver -> driver.noAutoCommit()));
	}
	
	/**
	 * Appends commit.
	 * @return new command
	 */
	public SQLTemplateBuilder commit() {
		return create(operation.andThen(driver -> driver.commit()));
	}
	
	/**
	 * Appends rollback.
	 * @return new command
	 */
	public SQLTemplateBuilder rollback() {
		return create(operation.andThen(driver -> driver.rollback()));
	}

	/**
	 * Appends prepare statement.
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public SQLTemplateBuilder statement(String sqlStmt) {
		return create(operation.andThen(driver -> driver.prepareStatement(sqlStmt)));
	}
	
	/**
	 * Appends update with arguments.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLTemplateBuilder update(Object[] params) {
		return create(operation.andThen(driver -> driver.executeUpdate(params)));
	}

	/**
	 * Appends query with arguments.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLTemplateBuilder query(Object[] params) {
		return create(operation.andThen(driver -> driver.executeQuery(params)));
	}

	/**
	 * Appends execute update.
	 * @return new command
	 */
	public SQLTemplateBuilder update() {
		return create(operation.andThen(driver -> driver.executeUpdate()));
	}

	/**
	 * Appends execute query.
	 * @return new command
	 */
	public SQLTemplateBuilder query() {
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

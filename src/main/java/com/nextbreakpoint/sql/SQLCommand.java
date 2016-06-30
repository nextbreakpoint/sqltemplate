/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.nextbreakpoint.Try;

/**
 * Represents a JDBC command.
 * 
 * @author Andrea
 *
 */
public class SQLCommand {
	private final SQLFunction function;

	private SQLCommand(SQLFunction function) {
		Objects.requireNonNull(function);
		this.function = function;
	}

	private static SQLCommand create(SQLFunction f) {
		return new SQLCommand(f);
	}

	/**
	 * Applies the command to given SQLDriver and returns the result as Try instance.
	 * @param driver the driver
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> apply(SQLDriver driver) {
		return function.apply(driver);
	}

	/**
	 * Creates a new empty command.
	 * @return new command
	 */
	public static SQLCommand begin() {
		return create(sql -> Try.success(SQLDriver.defaultMapper(), sql));
	}

	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand autoCommit() {
		return create(function.andThen(driver -> driver.autoCommit()));
	}

	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand noAutoCommit() {
		return create(function.andThen(driver -> driver.noAutoCommit()));
	}
	
	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand commit() {
		return create(function.andThen(driver -> driver.commit()));
	}
	
	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand rollback() {
		return create(function.andThen(driver -> driver.rollback()));
	}

	/**
	 * Concatenates a command with this command.
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public SQLCommand prepareStatement(String sqlStmt) {
		return create(function.andThen(driver -> driver.prepareStatement(sqlStmt)));
	}
	
	/**
	 * Concatenates a command with this command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLCommand execute(Object[] params) {
		return create(function.andThen(driver -> driver.execute(params)));
	}

	/**
	 * Concatenates a command with this command.
	 * @param params the parameters
	 * @return new command
	 */
	public SQLCommand executeQuery(Object[] params) {
		return create(function.andThen(driver -> driver.executeQuery(params)));
	}

	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand execute() {
		return create(function.andThen(driver -> driver.execute()));
	}

	/**
	 * Concatenates a command with this command.
	 * @return new command
	 */
	public SQLCommand executeQuery() {
		return create(function.andThen(driver -> driver.executeQuery()));
	}
}

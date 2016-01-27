/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.function.Consumer;

import com.nextbreakpoint.Try;

/**
 * Represents a JDBC command.
 * 
 * @author Andrea
 *
 */
@FunctionalInterface
public interface SQLCommand {
	/**
	 * Applies the command to given SQLTemplate and returns the result as Try instance.
	 * @param sql the SQLTemplate
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> apply(SQLTemplate sql);

	/**
	 * Creates a new empty command.
	 * @return new command
	 */
	public static SQLCommand begin() {
		return sql -> Try.success(SQLTemplate.defaultMapper(), sql);
	}

	/**
	 * Creates a new command from given command.
	 * @param command the command
	 * @return new command
	 */
	public static SQLCommand begin(SQLCommand command) {
		return sql -> command.apply(sql);
	}

	/**
	 * Concatenates a command with this command.
	 * @param command the command
	 * @return new command
	 */
	public default SQLCommand andThen(SQLCommand command) {
		return sql1 -> apply(sql1).flatMap(sql2 -> command.apply(sql2));
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.autoCommit()).
	 * @return new command
	 */
	public default SQLCommand autoCommit() {
		return this.andThen(sql -> sql.autoCommit());
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.noAutoCommit()).
	 * @return new command
	 */
	public default SQLCommand noAutoCommit() {
		return this.andThen(sql -> sql.noAutoCommit());
	}
	
	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.commit()).
	 * @return new command
	 */
	public default SQLCommand commit() {
		return this.andThen(sql -> sql.commit());
	}
	
	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.rollback()).
	 * @return new command
	 */
	public default SQLCommand rollback() {
		return this.andThen(sql -> sql.rollback());
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.prepareStatement(sqlStmt)).
	 * @param sqlStmt the SQL statement
	 * @return new command
	 */
	public default SQLCommand prepareStatement(String sqlStmt) {
		return this.andThen(sql -> sql.prepareStatement(sqlStmt));
	}
	
	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.execute(params)).
	 * @param params the parameters
	 * @return new command
	 */
	public default SQLCommand execute(Object[] params) {
		return this.andThen(sql -> sql.execute(params));
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.executeQuery(params)).
	 * @param params the parameters
	 * @return new command
	 */
	public default SQLCommand executeQuery(Object[] params) {
		return this.andThen(sql -> sql.executeQuery(params));
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.execute()).
	 * @return new command
	 */
	public default SQLCommand execute() {
		return this.andThen(sql -> sql.execute());
	}

	/**
	 * Concatenates a command with this command.
	 * Same as andThen(sql -&gt; sql.executeQuery()).
	 * @return new command
	 */
	public default SQLCommand executeQuery() {
		return this.andThen(sql -> sql.executeQuery());
	}

	/**
	 * Applies the command and consumes the result if present. 
	 * @param consumer the consumer
	 * @return the result
	 */
	public default SQLCommand peek(Consumer<SQLTemplate> consumer) {
		return sql -> {
			Try<SQLTemplate, SQLTemplateException> result = apply(sql);
			result.ifPresent(consumer);
			return result; 
		};
	}
}

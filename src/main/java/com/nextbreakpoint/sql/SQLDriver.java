/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.sql.SQLException;
import java.util.List;

import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an interface for executing JDBC operations.
 * 
 * @author Andrea
 *
 */
class SQLDriver {
	private final Connection conn;
	private final SQLResult sqlResult;
	private final SQLStatement sqlStatement;

	private SQLDriver(Connection conn) {
		this(conn, null, null);
	}

	private SQLDriver(Connection conn, SQLStatement sqlStatement, SQLResult sqlResult) {
		Objects.requireNonNull(conn);
		this.conn = conn;
		this.sqlResult = sqlResult;
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Attempts to enable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> autoCommit() {
		return tryCallable(() -> create(doAutoCommit(true), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to disable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> noAutoCommit() {
		return tryCallable(() -> create(doAutoCommit(false), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> commit() {
		return tryCallable(() -> create(doCommit(), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to rollback and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> rollback() {
		return tryCallable(() -> create(doRollback(), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to create a prepared statement and returns the result as Try instance.
	 * @param sql the SQL statement
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> prepareStatement(String sql) {
		return tryCallable(() -> create(conn, new SQLStatement(conn.prepareStatement(sql)), null));
	}

	/**
	 * Attempts to execute the current update statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeUpdate(Object[] params) {
		return tryCallable(() -> sqlStatement.executeUpdate(params).map(res -> create(conn, sqlStatement, SQLResult.of(res))).getOrThrow());
	}

	/**
	 * Attempts to execute the current query statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public <R> Try<SQLDriver, SQLTemplateException> executeQuery(Object[] params) {
		return tryCallable(() -> sqlStatement.executeQuery(params).map(res -> create(conn, sqlStatement, SQLResult.of(res))).getOrThrow());
	}

	/**
	 * Attempts to execute the current update statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeUpdate() {
		return executeUpdate((Object[])null);
	}

	/**
	 * Attempts to execute the current query statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeQuery() {
		return executeQuery((Object[])null);
	}

	/**
	 * Returns the result as list of arrays of objects.
	 * @return the list
	 */
	public List<Object[]> values() {
		return stream().collect(Collectors.toList());
	}

	/**
	 * Creates a new instance from given connection.
	 * @param conn the connection
	 * @return new instance
	 */
	public static SQLDriver create(Connection conn) {
		return new SQLDriver(conn);
	}

	/**
	 * Returns default mapper function. 
	 * @return the mapper
	 */
	public static Function<Throwable, SQLTemplateException> defaultMapper() {
		return e -> (e instanceof SQLTemplateException) ? (SQLTemplateException)e : new SQLTemplateException("SQL template error", e);
	}

	/**
	 * Attempts to execute the callable.
	 * @param <R> the value type
	 * @return the result
	 */
	public static <R> Try<R, SQLTemplateException> tryCallable(Callable<R> callable) {
		return Try.of(defaultMapper(), callable);
	}

	private static SQLDriver create(Connection conn, SQLStatement sqlStatement, SQLResult sqlResult) {
		return new SQLDriver(conn, sqlStatement, sqlResult);
	}

	private Stream<Object[]> stream() {
		return Optional.ofNullable(sqlResult).map(s -> s.stream()).orElse(Stream.empty());
	}

	private Connection doAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
		return conn;
	}

	private Connection doCommit() throws SQLException {
		conn.commit();
		return conn;
	}

	private Connection doRollback() throws SQLException {
		conn.rollback();
		return conn;
	}
}

/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;
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
		return tryCallable(() -> {
			conn.setAutoCommit(true);
			conn.commit();
			return create(conn, sqlStatement, sqlResult);
		});
	}

	/**
	 * Attempts to disable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> noAutoCommit() {
		return tryCallable(() -> {
			conn.setAutoCommit(false);
			conn.commit();
			return create(conn, sqlStatement, sqlResult);
		});
	}

	/**
	 * Attempts to commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> commit() {
		return tryCallable(() -> {
			conn.commit();
			return create(conn, sqlStatement, sqlResult);
		});
	}

	/**
	 * Attempts to rollback and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> rollback() {
		return tryCallable(() -> {
			conn.rollback();
			return create(conn, sqlStatement, sqlResult);
		});
	}

	/**
	 * Attempts to create a prepared statement and returns the result as Try instance.
	 * @param sql the SQL statement
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> prepareStatement(String sql) {
		return tryCallable(() -> {
			return create(conn, new SQLStatement(conn.prepareStatement(sql)), null);
		});
	}

	/**
	 * Attempts to executeUpdate the current statement create given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeUpdate(Object[] params) {
		return Optional.ofNullable(sqlStatement).map(st -> st.execute(params))
			.map(res -> res.flatMap(cnt -> success(create(conn, sqlStatement, SQLResult.of(cnt)))))
			.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	/**
	 * Attempts to executeUpdate the current query statement create given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeQuery(Object[] params) {
		return Optional.ofNullable(sqlStatement).map(st -> st.executeQuery(params))
			.map(res -> res.flatMap(set -> success(create(conn, sqlStatement, SQLResult.of(set)))))
			.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	/**
	 * Attempts to executeUpdate the current statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeUpdate() {
		return executeUpdate((Object[])null);
	}

	/**
	 * Attempts to executeUpdate the current query statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeQuery() {
		return executeQuery((Object[])null);
	}

	/**
	 * Returns the result as list create array create objects.
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

	static <T> Try<T, SQLTemplateException> success(T value) {
		return Try.success(defaultMapper(), value);
	}

	static <T> Try<T, SQLTemplateException> failure(Exception exception) {
		return Try.failure(defaultMapper(), defaultMapper().apply(exception));
	}

	/**
	 * Returns default mapper function. 
	 * @return the mapper
	 */
	public static Function<Throwable, SQLTemplateException> defaultMapper() {
		return e -> (e instanceof SQLTemplateException) ? (SQLTemplateException)e : new SQLTemplateException("SQL template error", e);
	}

	private static SQLDriver create(Connection conn, SQLStatement sqlStatement, SQLResult sqlResult) {
		return new SQLDriver(conn, sqlStatement, sqlResult);
	}

	private Stream<Object[]> stream() {
		return Optional.ofNullable(sqlResult).map(s -> s.stream()).orElse(Stream.empty());
	}

	/**
	 * Tries to execute the callable.
	 * @param <R> the value type
	 * @return the result
	 */
	public static <R> Try<R, SQLTemplateException> tryCallable(Callable<R> callable) {
		return Try.of(defaultMapper(), callable);
	}
}

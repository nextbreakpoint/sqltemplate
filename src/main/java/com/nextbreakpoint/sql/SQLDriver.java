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
	private final Optional<SQLResult> sqlResult;
	private final Optional<SQLStatement> sqlStatement;

	private SQLDriver(Connection conn) {
		this(conn, Optional.empty(), Optional.empty());
	}

	private SQLDriver(Connection conn, Optional<SQLStatement> sqlStatement, Optional<SQLResult> sqlResult) {
		Objects.requireNonNull(conn);
		Objects.requireNonNull(sqlResult);
		Objects.requireNonNull(sqlStatement);
		this.conn = conn;
		this.sqlResult = sqlResult;
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Attempts to enable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> autoCommit() {
		try {
			conn.setAutoCommit(true);
			conn.commit();
			return success(new SQLDriver(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to disable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> noAutoCommit() {
		try {
			conn.setAutoCommit(false);
			conn.commit();
			return success(new SQLDriver(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> commit() {
		try {
			conn.commit();
			return success(new SQLDriver(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to rollback and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> rollback() {
		try {
			conn.rollback();
			return success(new SQLDriver(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to create a prepared statement and returns the result as Try instance.
	 * @param sql the SQL statement
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> prepareStatement(String sql) {
		try {
			return success(new SQLDriver(conn, Optional.of(new SQLStatement(conn.prepareStatement(sql))), Optional.empty()));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to executeUpdate the current statement create given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeUpdate(Object[] params) {
		return sqlStatement.map(st -> st.execute(params))
				.map(res -> res.flatMap(cnt -> success(new SQLDriver(conn, sqlStatement, Optional.of(SQLResult.of(cnt))))))
				.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	/**
	 * Attempts to executeUpdate the current query statement create given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLDriver, SQLTemplateException> executeQuery(Object[] params) {
		return sqlStatement.map(st -> st.executeQuery(params))
				.map(res -> res.flatMap(set -> success(new SQLDriver(conn, sqlStatement, Optional.of(SQLResult.of(set))))))
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

	private Stream<Object[]> stream() {
		return sqlResult.map(s -> s.stream()).orElse(Stream.empty());
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

	static <T> Try<T, SQLTemplateException> of(Callable<T> callable) {
		return Try.of(defaultMapper(), callable);
	}

	/**
	 * Returns default mapper function. 
	 * @return the mapper
	 */
	public static Function<Throwable, SQLTemplateException> defaultMapper() {
		return e -> (e instanceof SQLTemplateException) ? (SQLTemplateException)e : new SQLTemplateException("SQL template error", e);
	}
}

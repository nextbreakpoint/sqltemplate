/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import com.nextbreakpoint.Try;

/**
 * SQLTemplate implements a functional API for executing SQL statements using JDBC in Java 8.
 * 
 * @author Andrea
 *
 */
public class SQLTemplate {
	private final Connection conn;
	private final Optional<SQLResult> sqlResult;
	private final Optional<SQLStatement> sqlStatement;
	
	private SQLTemplate(Connection conn) {
		this(conn, Optional.empty(), Optional.empty());
	}

	private SQLTemplate(Connection conn, Optional<SQLStatement> sqlStatement, Optional<SQLResult> sqlResult) {
		Objects.requireNonNull(conn);
		Objects.requireNonNull(sqlResult);
		Objects.requireNonNull(sqlStatement);
		this.conn = conn;
		this.sqlResult = sqlResult;
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Attempts to execute the given command and returns the result as Try instance.  
	 * @param command the command
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> execute(SQLCommand command) {
		return command.apply(this);
	}

	/**
	 * Attempts to enable auto commit and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> autoCommit() {
		try {
			conn.setAutoCommit(true);
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to disable auto commit and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> noAutoCommit() {
		try {
			conn.setAutoCommit(false);
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	/**
	 * Attempts to commit and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> commit() {
		try {
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	/**
	 * Attempts to rollback and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> rollback() {
		try {
			conn.rollback();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	/**
	 * Attempts to create a prepared statement and returns the result as Try instance.
	 * @param sql the SQL statement  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> prepareStatement(String sql) {
		try {
			return success(new SQLTemplate(conn, Optional.of(new SQLStatement(conn.prepareStatement(sql))), Optional.empty()));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	/**
	 * Attempts to execute the current statement with given parameters and returns the result as Try instance.  
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> execute(Object[] params) {
		return sqlStatement.map(st -> st.execute(params))
				.map(res -> res.flatMap(cnt -> success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(cnt))))))
				.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	/**
	 * Attempts to execute the current query statement with given parameters and returns the result as Try instance.  
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> executeQuery(Object[] params) {
		return sqlStatement.map(st -> st.executeQuery(params))
				.map(res -> res.flatMap(set -> success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(set))))))
				.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	/**
	 * Attempts to execute the current statement and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> execute() {
		return execute((Object[])null);
	}

	/**
	 * Attempts to execute the current query statement and returns the result as Try instance.  
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> executeQuery() {
		return executeQuery((Object[])null);
	}

	/**
	 * Returns the result as stream of array of objects.  
	 * @return the stream
	 */
	public Stream<Object[]> stream() {
		return sqlResult.map(s -> s.stream()).orElse(Stream.empty());
	}

	/**
	 * Creates new instance from given connection. 
	 * @param conn the connection
	 * @return new instance
	 */
	public static SQLTemplate create(Connection conn) {
		return new SQLTemplate(conn);
	}

	/**
	 * Creates Try instance from given value. 
	 * @param value the value
	 * @param <T> the type of result value
	 * @return the result
	 */
	public static <T> Try<T, SQLTemplateException> success(T value) {
		return Try.success(defaultMapper(), value);
	}

	/**
	 * Creates Try instance from given exception. 
	 * @param exception the exception
	 * @param <T> the type of result value
	 * @return the result
	 */
	public static <T> Try<T, SQLTemplateException> failure(Exception exception) {
		return Try.failure(defaultMapper(), defaultMapper().apply(exception));
	}
	
	/**
	 * Creates Try instance from given callable. 
	 * @param callable the callable
	 * @param <T> the type of result value
	 * @return the result
	 */
	public static <T> Try<T, SQLTemplateException> of(Callable<T> callable) {
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

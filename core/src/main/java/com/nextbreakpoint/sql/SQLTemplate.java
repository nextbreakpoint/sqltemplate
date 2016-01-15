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

	public Try<SQLTemplate, SQLTemplateException> execute(SQLCommand cmd) {
		return cmd.apply(this);
	}

	public Try<SQLTemplate, SQLTemplateException> autoCommit() {
		try {
			conn.setAutoCommit(true);
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	public Try<SQLTemplate, SQLTemplateException> noAutoCommit() {
		try {
			conn.setAutoCommit(false);
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	public Try<SQLTemplate, SQLTemplateException> commit() {
		try {
			conn.commit();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	public Try<SQLTemplate, SQLTemplateException> rollback() {
		try {
			conn.rollback();
			return success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return failure(e);
		}
	}

	public Try<SQLTemplate, SQLTemplateException> prepareStatement(String sql) {
		try {
			return success(new SQLTemplate(conn, Optional.of(new SQLStatement(conn.prepareStatement(sql))), Optional.empty()));
		} catch (Exception e) {
			return failure(e);
		}
	}
	
	public Try<SQLTemplate, SQLTemplateException> execute(Object[] params) {
		return sqlStatement.map(st -> st.execute(params))
				.map(res -> res.flatMap(cnt -> success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(cnt))))))
				.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	public Try<SQLTemplate, SQLTemplateException> executeQuery(Object[] params) {
		return sqlStatement.map(st -> st.executeQuery(params))
				.map(res -> res.flatMap(set -> success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(set))))))
				.orElseGet(() -> failure(new SQLTemplateException("statement not found")));
	}

	public Try<SQLTemplate, SQLTemplateException> execute() {
		return execute((Object[])null);
	}

	public Try<SQLTemplate, SQLTemplateException> executeQuery() {
		return executeQuery((Object[])null);
	}

	public Stream<Object[]> stream() {
		return sqlResult.map(s -> s.stream()).orElse(Stream.empty());
	}

	public static SQLTemplate create(Connection conn) {
		return new SQLTemplate(conn);
	}

	public static <T> Try<T, SQLTemplateException> success(T template) {
		return Try.success(wrapException(), template);
	}

	public static <T> Try<T, SQLTemplateException> failure(Exception exception) {
		return Try.failure(wrapException(), wrapException().apply(exception));
	}
	
	public static <T> Try<T, SQLTemplateException> of(Callable<T> callable) {
		return Try.of(wrapException(), callable);
	}

	public static Function<Throwable, SQLTemplateException> wrapException() {
		return e -> (e instanceof SQLTemplateException) ? (SQLTemplateException)e : new SQLTemplateException("SQL template error", e);
	}
}
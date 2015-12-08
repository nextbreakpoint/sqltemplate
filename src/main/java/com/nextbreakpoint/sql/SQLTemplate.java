package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;
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
		Objects.requireNonNull(sqlStatement);
		Objects.requireNonNull(sqlResult);
		this.conn = conn;
		this.sqlResult = sqlResult;
		this.sqlStatement = sqlStatement;
	}

	public Try<SQLTemplate> execute(SQLCommand cmd) {
		return cmd.apply(this);
	}

	public Try<SQLTemplate> autoCommit() {
		try {
			conn.setAutoCommit(true);
			conn.commit();
			return Try.success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return Try.failure(e);
		}
	}

	public Try<SQLTemplate> noAutoCommit() {
		try {
			conn.setAutoCommit(false);
			conn.commit();
			return Try.success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return Try.failure(e);
		}
	}
	
	public Try<SQLTemplate> commit() {
		try {
			conn.commit();
			return Try.success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return Try.failure(e);
		}
	}
	
	public Try<SQLTemplate> rollback() {
		try {
			conn.rollback();
			return Try.success(new SQLTemplate(conn, sqlStatement, sqlResult));
		} catch (Exception e) {
			return Try.failure(e);
		}
	}

	public Try<SQLTemplate> prepareStatement(String sql) {
		try {
			return Try.success(new SQLTemplate(conn, Optional.of(new SQLStatement(conn.prepareStatement(sql))), Optional.empty()));
		} catch (Exception e) {
			return Try.failure(e);
		}
	}
	
	public Try<SQLTemplate> execute(Object[] params) {
		return sqlStatement.map(st -> st.execute(params))
				.map(res -> res.flatMap(cnt -> Try.success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(cnt))))))
				.orElse(Try.success(new SQLTemplate(conn, sqlStatement, Optional.empty())));
	}

	public Try<SQLTemplate> executeQuery(Object[] params) {
		return sqlStatement.map(st -> st.executeQuery(params))
				.map(res -> res.flatMap(set -> Try.success(new SQLTemplate(conn, sqlStatement, Optional.of(SQLResult.of(set))))))
				.orElse(Try.success(new SQLTemplate(conn, sqlStatement, Optional.empty())));
	}

	public Try<SQLTemplate> execute() {
		return execute((Object[])null);
	}

	public Try<SQLTemplate> executeQuery() {
		return executeQuery((Object[])null);
	}

	public Stream<Object[]> stream() {
		return sqlResult.map(s -> s.stream()).orElse(Stream.empty());
	}

	public static SQLTemplate create(Connection conn) {
		return new SQLTemplate(conn);
	}
}

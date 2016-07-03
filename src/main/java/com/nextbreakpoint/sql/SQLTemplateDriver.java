/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides a driver for executing JDBC operations.
 * 
 * @author Andrea Medeghini
 *
 */
public class SQLTemplateDriver {
	private final Connection conn;
	private final SQLResult sqlResult;
	private final SQLStatement sqlStatement;

	private SQLTemplateDriver(Connection conn) {
		this(conn, null, null);
	}

	private SQLTemplateDriver(Connection conn, SQLStatement sqlStatement, SQLResult sqlResult) {
		Objects.requireNonNull(conn);
		this.conn = conn;
		this.sqlResult = sqlResult;
		this.sqlStatement = sqlStatement;
	}

	/**
	 * Attempts to enable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> autoCommit() {
		return tryCallable(() -> create(doAutoCommit(true), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to disable auto commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> noAutoCommit() {
		return tryCallable(() -> create(doAutoCommit(false), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to commit and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> commit() {
		return tryCallable(() -> create(doCommit(), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to rollback and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> rollback() {
		return tryCallable(() -> create(doRollback(), sqlStatement, sqlResult));
	}

	/**
	 * Attempts to create a prepared statement and returns the result as Try instance.
	 * @param sql the SQL statement
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> prepareStatement(String sql) {
		return tryCallable(() -> create(conn, new SQLStatement(conn.prepareStatement(sql)), null));
	}

	/**
	 * Attempts to execute the current update statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> executeUpdate(Object[] params) {
		return tryCallable(() -> sqlStatement.executeUpdate(params).map(res -> create(conn, sqlStatement, SQLResult.of(res))).getOrThrow());
	}

	/**
	 * Attempts to execute the current query statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> executeQuery(Object[] params) {
		return tryCallable(() -> sqlStatement.executeQuery(params).map(res -> create(conn, sqlStatement, SQLResult.of(res))).getOrThrow());
	}

	/**
	 * Attempts to execute the current update statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> executeUpdate() {
		return executeUpdate((Object[])null);
	}

	/**
	 * Attempts to execute the current query statement and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> executeQuery() {
		return executeQuery((Object[])null);
	}

	/**
	 * Attempts to fetch data from current result set and returns the result as Try instance.
	 * @return the result
	 */
	public Try<SQLTemplateDriver, SQLTemplateException> fetch() {
		return tryCallable(() -> create(conn, sqlStatement, SQLResult.of(values())));
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
	public static SQLTemplateDriver create(Connection conn) {
		return new SQLTemplateDriver(conn);
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
	 * @param callable the callable
	 * @param <R> the value type
	 * @return the result
	 */
	public static <R> Try<R, SQLTemplateException> tryCallable(Callable<R> callable) {
		return Try.of(defaultMapper(), callable);
	}

	private static SQLTemplateDriver create(Connection conn, SQLStatement sqlStatement, SQLResult sqlResult) {
		return new SQLTemplateDriver(conn, sqlStatement, sqlResult);
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

	private static class SQLStatement {
		private final PreparedStatement st;

		public SQLStatement(PreparedStatement st) {
			Objects.requireNonNull(st);
			this.st = st;
		}

		@Override
		protected void finalize() throws Throwable {
			if (st != null) {
				st.close();
			}
			super.finalize();
		}

		public Try<Integer, SQLTemplateException> executeUpdate(Object[] params) {
			return SQLTemplateDriver.tryCallable(() -> bindParameters(params).executeUpdate());
		}

		public Try<ResultSet, SQLTemplateException> executeQuery(Object[] params) {
			return SQLTemplateDriver.tryCallable(() -> bindParameters(params).executeQuery());
		}

		private PreparedStatement bindParameters(Object[] params) throws Exception {
			if (params != null) for (int p = 1; p <= params.length; p++) st.setObject(p, params[p - 1]);
			return st;
		}
	}

	private static abstract class SQLResult {
		public abstract Stream<Object[]> stream();

		public static SQLResult of(List<Object[]> list) {
			return new SQLResult.SQLResultList(list);
		}

		public static SQLResult of(ResultSet rs) {
			return new SQLResult.SQLResultQuery(rs);
		}

		public static SQLResult of(Integer result) {
			return new SQLResult.SQLResultUpdate(result);
		}

		private static class SQLResultList extends SQLResult {
			private final List<Object[]> list;

			public SQLResultList(List<Object[]> list) {
				Objects.requireNonNull(list);
				this.list = list;
			}

			@Override
			public Stream<Object[]> stream() {
				return list.stream();
			}
		}

		private static class SQLResultQuery extends SQLResult {
			private final ResultSet rs;

			public SQLResultQuery(ResultSet rs) {
				Objects.requireNonNull(rs);
				this.rs = rs;
			}

			@Override
			protected void finalize() throws Throwable {
				if (rs != null) {
					rs.close();
				}
				super.finalize();
			}

			public Stream<Object[]> stream() {
				return StreamSupport.stream(new SQLResult.SQLResultQuery.ResultSpliterator(), false);
			}

			private class ResultSpliterator implements Spliterator<Object[]> {
				@Override
				public boolean tryAdvance(Consumer<? super Object[]> consumer) {
					return Try.of(() -> {
						if (rs.next()) {
							ResultSetMetaData metadata = rs.getMetaData();
							Object[] columns = new Object[metadata.getColumnCount()];
							bindColumns(rs, columns);
							consumer.accept(columns);
							return true;
						} else {
							return false;
						}
					}).getOrElse(false);
				}

				private void bindColumns(ResultSet rs, Object[] columns) throws Exception {
					for (int i = 0; i < columns.length; i++) columns[i] = rs.getObject(i + 1);
				}

				@Override
				public Spliterator<Object[]> trySplit() {
					return null;
				}

				@Override
				public long estimateSize() {
					return Long.MAX_VALUE;
				}

				@Override
				public int characteristics() {
					return Spliterator.IMMUTABLE | Spliterator.NONNULL;
				}
			}
		}

		private static class SQLResultUpdate extends SQLResult {
			private final long value;
			private boolean consumed;

			public SQLResultUpdate(long value) {
				this.value = value;
			}

			public Stream<Object[]> stream() {
				return StreamSupport.stream(new SQLResult.SQLResultUpdate.ResultSpliterator(), false);
			}

			private class ResultSpliterator implements Spliterator<Object[]> {
				@Override
				public boolean tryAdvance(Consumer<? super Object[]> consumer) {
					if (!consumed) {
						consumer.accept(new Object[] { value });
						consumed = true;
						return true;
					}
					return false;
				}

				@Override
				public Spliterator<Object[]> trySplit() {
					return null;
				}

				@Override
				public long estimateSize() {
					return 1;
				}

				@Override
				public int characteristics() {
					return Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.NONNULL;
				}
			}
		}
	}
}

/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Encapsulates a JDBC ResultSet.
 * 
 * @author Andrea
 *
 */
abstract class SQLResult {
	/**
	 * Creates a stream from the result set.
	 * @return the stream
	 */
	public abstract Stream<Object[]> stream();

	/**
	 * Creates a new instance from given result set.
	 * @param rs the result set
	 * @return new instance
	 */
	public static SQLResult of(ResultSet rs) {
		return new SQLResultQuery(rs);
	}
	
	/**
	 * Creates a new instance from given integer value.
	 * @param result the integer value
	 * @return new instance
	 */
	public static SQLResult of(Integer result) {
		return new SQLResultUpdate(result);
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
		}

		public Stream<Object[]> stream() {
			return StreamSupport.stream(new ResultSpliterator(), false);
		}

		private class ResultSpliterator implements Spliterator<Object[]> {
			@Override
			public boolean tryAdvance(Consumer<? super Object[]> action) {
				try {
					if (rs != null && rs.next()) {
						ResultSetMetaData metadata = rs.getMetaData();
						Object[] columns = new Object[metadata.getColumnCount()];
						bindColumns(rs, columns);
						action.accept(columns);
						return true;
					}
				} catch (Exception e) {
				}
				return false;
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
			return StreamSupport.stream(new ResultSpliterator(), false);
		}

		private class ResultSpliterator implements Spliterator<Object[]> {
			@Override
			public boolean tryAdvance(Consumer<? super Object[]> action) {
				if (!consumed) {
					action.accept(new Object[] { value });
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

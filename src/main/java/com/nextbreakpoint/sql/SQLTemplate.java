/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

/**
 * SQLTemplate implements a fluent interface for executing SQL statements.
 * 
 * @author Andrea Medeghini
 *
 */
public class SQLTemplate {
	private final SQLOperation operation;

	private SQLTemplate(SQLOperation operation) {
		this.operation = Objects.requireNonNull(operation);
	}

	/**
	 * Creates a Try monad with operations defined in the template.
	 * @param connection a JDBC connection
	 * @return the monad
	 */
	public Try<List<Object[]>, SQLTemplateException> apply(Connection connection) {
		return operation.apply(SQLTemplateDriver.create(connection))
				.flatMap(SQLTemplateDriver::fetch).map(SQLTemplateDriver::values).execute();
	}

	/**
	 * Creates a new builder.
	 * @return the builder
	 */
	public static SQLTemplateBuilder builder() {
		return new SQLTemplateBuilder(driver -> Try.success(driver).mapper(SQLTemplateDriver.defaultMapper()));
	}

	public static class SQLTemplateBuilder {
		private final SQLOperation operation;

		private SQLTemplateBuilder(SQLOperation operation) {
			this.operation = Objects.requireNonNull(operation);
		}

		/**
		 * Appends operation set auto commit true.
		 * @return the builder
		 */
		public SQLTemplateBuilder autoCommit() {
			return create(operation.andThen(driver -> driver.autoCommit()));
		}

		/**
		 * Appends operation set auto commit false.
		 * @return the builder
		 */
		public SQLTemplateBuilder noAutoCommit() {
			return create(operation.andThen(driver -> driver.noAutoCommit()));
		}

		/**
		 * Appends operation commit.
		 * @return the builder
		 */
		public SQLTemplateBuilder commit() {
			return create(operation.andThen(driver -> driver.commit()));
		}

		/**
		 * Appends operation rollback.
		 * @return the builder
		 */
		public SQLTemplateBuilder rollback() {
			return create(operation.andThen(driver -> driver.rollback()));
		}

		/**
		 * Appends operation prepare statement.
		 * @param sqlStmt the SQL statement
		 * @return the builder
		 */
		public SQLTemplateBuilder statement(String sqlStmt) {
			return create(operation.andThen(driver -> driver.prepareStatement(sqlStmt)));
		}

		/**
		 * Appends operation update with arguments.
		 * @param params the parameters
		 * @return the builder
		 */
		public SQLTemplateBuilder update(Object[] params) {
			return create(operation.andThen(driver -> driver.executeUpdate(params)));
		}

		/**
		 * Appends operation query with arguments.
		 * @param params the parameters
		 * @return the builder
		 */
		public SQLTemplateBuilder query(Object[] params) {
			return create(operation.andThen(driver -> driver.executeQuery(params)));
		}

		/**
		 * Appends operation update.
		 * @return the builder
		 */
		public SQLTemplateBuilder update() {
			return create(operation.andThen(driver -> driver.executeUpdate()));
		}

		/**
		 * Appends operation query.
		 * @return the builder
		 */
		public SQLTemplateBuilder query() {
			return create(operation.andThen(driver -> driver.executeQuery()));
		}

		/**
		 * Builds a template from sequence of operations.
		 * @return new template
		 */
		public SQLTemplate build() {
			return new SQLTemplate(operation);
		}

		private SQLTemplateBuilder create(SQLOperation operation) {
			return new SQLTemplateBuilder(operation);
		}
	}
}

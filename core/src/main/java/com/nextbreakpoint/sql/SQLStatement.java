/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

import com.nextbreakpoint.Try;

/**
 * Encapsulates a prepared statement.
 * 
 * @author Andrea
 *
 */
public class SQLStatement {
	private final PreparedStatement st;

	/**
	 * Creates new instance from given prepared statement.
	 * @param st the prepared statement
	 */
	public SQLStatement(PreparedStatement st) {
		Objects.requireNonNull(st);
		this.st = st;
	}

	@Override
	protected void finalize() throws Throwable {
		if (st != null) {
			st.close();
		}
	}

	/**
	 * Executes statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<Integer, SQLTemplateException> execute(Object[] params) {
		try {
			bindParameters(params);
			return SQLTemplate.success(st.executeUpdate());
		} catch (Exception e) {
			return SQLTemplate.failure(e);
		}
	}

	/**
	 * Executes query statement with given parameters and returns the result as Try instance.
	 * @param params the parameters
	 * @return the result
	 */
	public Try<ResultSet, SQLTemplateException> executeQuery(Object[] params) {
		try {
			bindParameters(params);
			return SQLTemplate.success(st.executeQuery());
		} catch (Exception e) {
			return SQLTemplate.failure(e);
		}
	}

	private void bindParameters(Object[] params) throws Exception {
		if (params != null) for (int p = 1; p <= params.length; p++) st.setObject(p, params[p - 1]);
	}
}

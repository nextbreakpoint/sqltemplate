/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.util.Objects;

import com.nextbreakpoint.Try;
import java.util.List;

/**
 * SQLTemplate implements a functional API for executing SQL statements using JDBC in Java 8.
 * 
 * @author Andrea
 *
 */
public class SQLTemplate {
	private final Connection conn;

	private SQLTemplate(Connection conn) {
		Objects.requireNonNull(conn);
		this.conn = conn;
	}

	/**
	 * Attempts to execute the given command and returns the result as Try instance.  
	 * @param command the command
	 * @return the result
	 */
	public Try<List<Object[]>, SQLTemplateException> execute(SQLCommand command) {
		return command.apply(SQLDriver.create(conn)).map(driver -> driver.values());
	}

	/**
	 * Creates new instance from given connection. 
	 * @param conn the connection
	 * @return new instance
	 */
	public static SQLTemplate with(Connection conn) {
		return new SQLTemplate(conn);
	}
}

/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.util.Objects;
import java.util.stream.Stream;

import com.nextbreakpoint.Try;
import java.util.List;

/**
 * SQLTemplate implements a functional API for executing SQL statements using JDBC in Java 8.
 * 
 * @author Andrea
 *
 */
public class SQLTemplate {
	private final SQLDriver driver;

	private SQLTemplate(SQLDriver driver) {
		Objects.requireNonNull(driver);
		this.driver = driver;
	}

	/**
	 * Attempts to execute the given command and returns the result as Try instance.  
	 * @param command the command
	 * @return the result
	 */
	public Try<SQLTemplate, SQLTemplateException> execute(SQLCommand command) {
		return command.apply(driver).map(driver -> new SQLTemplate(driver));
	}

	/**
	 * Creates new instance from given connection. 
	 * @param conn the connection
	 * @return new instance
	 */
	public static SQLTemplate of(Connection conn) {
		return new SQLTemplate(SQLDriver.create(conn));
	}

	/**
	 * Returns the result as list of array of objects.
	 * @return the list
	 */
	public List<Object[]> get() {
		return driver.values();
	}
}

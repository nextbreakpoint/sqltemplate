/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nextbreakpoint.Try;

public class SQLTemplateIT {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private Connection conn;
	
	@BeforeClass
	public static void setupDatabase() {
		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setupConnection() throws Exception {
		conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		conn.prepareStatement("DELETE TEST").executeUpdate();
	}

	@After
	public void cleanup() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}

	@Test
	public void execute_givenCommandCreateATableAndInsertTwoRowsAndSelectAll_shouldReturnSuccess() throws Exception {
		SQLCommand cmd = newTestCommand(); 
		Try<List<Object[]>, SQLTemplateException> sqlTemplate = SQLTemplate.with(conn).execute(cmd);
		assertTrue(sqlTemplate.isPresent());
	}

	@Test
	public void get_givenCommandCreateATableAndInsertTwoRowsAndSelectAll_shouldReturnStream() throws Exception {
		SQLCommand cmd = newTestCommand(); 
		Try<List<Object[]>, SQLTemplateException> sqlTemplate = SQLTemplate.with(conn).execute(cmd);
		assertNotNull(sqlTemplate.get());
	}

	@Test
	public void get_givenCommandCreateATableAndInsertTwoRowsAndSelectAll_shouldReturnTwoRows() throws Exception {
		SQLCommand cmd = newTestCommand(); 
		Try<List<Object[]>, SQLTemplateException> sqlTemplate = SQLTemplate.with(conn).execute(cmd);
		assertEquals(2, sqlTemplate.get().size());
	}

	@Test
	public void execute_givenCommandContainsErrorInStatement_shouldReturnFailure() throws Exception {
		SQLCommand cmd = newTestCommandWithErrorInStatement(); 
		Try<List<Object[]>, SQLTemplateException> sqlTemplate = SQLTemplate.with(conn).execute(cmd);
		assertTrue(sqlTemplate.isFailure());
	}

	@Test
	public void execute_givenCommandContainsErrorInParameters_shouldReturnFailure() throws Exception {
		SQLCommand cmd = newTestCommandWithErrorInParameters(); 
		Try<List<Object[]>, SQLTemplateException> sqlTemplate = SQLTemplate.with(conn).execute(cmd);
		assertTrue(sqlTemplate.isFailure());
	}

	private SQLCommand newTestCommand() {
		return SQLCommand.begin()
			.noAutoCommit() 
			.prepareStatement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.execute() 
			.prepareStatement("DELETE TEST")
			.execute() 
			.prepareStatement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.execute(new Object[] { 1, "A" })
			.execute(new Object[] { 2, "B" })
			.commit() 
			.prepareStatement("SELECT * FROM TEST")
			.executeQuery();
	}
 
	private SQLCommand newTestCommandWithErrorInStatement() {
		return SQLCommand.begin()
			.noAutoCommit() 
			.prepareStatement("CREAT TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.execute() 
			.prepareStatement("DELETE TEST")
			.execute() 
			.prepareStatement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.execute(new Object[] { 1, "A" })
			.execute(new Object[] { 2, "B" })
			.commit() 
			.prepareStatement("SELECT * FROM TEST")
			.executeQuery();
	}

	private SQLCommand newTestCommandWithErrorInParameters() {
		return SQLCommand.begin()
			.noAutoCommit() 
			.prepareStatement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.execute() 
			.prepareStatement("DELETE TEST")
			.execute() 
			.prepareStatement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.execute(new Object[] { 1, "A" })
			.execute(new Object[] { "A", "B" })
			.commit() 
			.prepareStatement("SELECT * FROM TEST")
			.executeQuery();
	}
}

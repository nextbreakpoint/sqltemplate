/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

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

import static org.junit.Assert.*;

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
	public void shouldReturnSuccess() throws Exception {
		SQLTemplate template = templateWithValidStatement();
		Try<List<Object[]>, SQLTemplateException> result = template.apply(conn);
		assertFalse(result.isFailure());
	}

	@Test
	public void shouldReturnResult() throws Exception {
		SQLTemplate template = templateWithValidStatement();
		Try<List<Object[]>, SQLTemplateException> result = template.apply(conn);
		assertNotNull(result.get());
	}

	@Test
	public void shouldReturnTwoRows() throws Exception {
		SQLTemplate template = templateWithValidStatement();
		Try<List<Object[]>, SQLTemplateException> result = template.apply(conn);
		assertEquals(2, result.get().size());
	}

	@Test
	public void shouldReturnFailureWhenErrorInStatement() throws Exception {
		SQLTemplate template = templateWithErrorInStatement();
		Try<List<Object[]>, SQLTemplateException> result = template.apply(conn);
		assertTrue(result.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenErrorInParameters() throws Exception {
		SQLTemplate template = templateWithErrorInParameters();
		Try<List<Object[]>, SQLTemplateException> result = template.apply(conn);
		assertTrue(result.isFailure());
	}

	private SQLTemplate templateWithValidStatement() {
		return SQLTemplate.builder()
			.noAutoCommit() 
			.statement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.update()
			.statement("DELETE TEST")
			.update()
			.statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.update(new Object[] { 1, "A" })
			.update(new Object[] { 2, "B" })
			.commit() 
			.statement("SELECT * FROM TEST")
			.query()
			.build();
	}
 
	private SQLTemplate templateWithErrorInStatement() {
		return SQLTemplate.builder()
			.noAutoCommit() 
			.statement("CREAT TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.update()
			.statement("DELETE TEST")
			.update()
			.statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.update(new Object[] { 1, "A" })
			.update(new Object[] { 2, "B" })
			.commit() 
			.statement("SELECT * FROM TEST")
			.query()
			.build();
	}

	private SQLTemplate templateWithErrorInParameters() {
		return SQLTemplate.builder()
			.noAutoCommit() 
			.statement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
			.update()
			.statement("DELETE TEST")
			.update()
			.statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
			.update(new Object[] { 1, "A" })
			.update(new Object[] { "A", "B" })
			.commit() 
			.statement("SELECT * FROM TEST")
			.query()
			.build();
	}
}

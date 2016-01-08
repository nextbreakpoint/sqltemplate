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
	public void get_givenCommandCreateATableAndInsertTwoRowsAndSelectAll_shouldReturnTwoRows() throws Exception {
		SQLCommand cmd = SQLCommand.begin()
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

		Try<SQLTemplate, SQLTemplateException> sqlTemplate = SQLTemplate.create(conn).execute(cmd);
		
		assertTrue(sqlTemplate.isPresent());

		SQLTemplate result = sqlTemplate.get();
		
		assertNotNull(result);
		assertEquals(2, result.stream().count());
	}
}

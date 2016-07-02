/*
 * This file is part of SQLDriver
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SQLTemplateDriverTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void shouldThrowException() {
		exception.expect(NullPointerException.class);
		SQLTemplateDriver.create(null);
	}

	@Test
	public void shouldNotReturnNull() {
		Connection conn = mock(Connection.class);
		assertNotNull(SQLTemplateDriver.create(conn));
	}

	@Test
	public void shouldCallSetAutoCommitWithFalse() throws Exception {
		Connection conn = mock(Connection.class);
		SQLTemplateDriver.create(conn).noAutoCommit();
		verify(conn, times(1)).setAutoCommit(false);
	}

	@Test
	public void shouldCallSetAutoCommitWithTrue() throws Exception {
		Connection conn = mock(Connection.class);
		SQLTemplateDriver.create(conn).autoCommit();
		verify(conn, times(1)).setAutoCommit(true);
	}

	@Test
	public void shouldCallPrepareStatement() throws Exception {
		Connection conn = mock(Connection.class);
		String stmtSql = "select * from test";
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql);
		verify(conn, times(1)).prepareStatement(stmtSql);
	}

	@Test
	public void shouldCallExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "delete from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeUpdate();
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQuery() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeQuery();
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldCallExecuteUpdateWithArguments() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "delete from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeUpdate(new Object[] { 1L });
		verify(stmt, times(1)).setObject(1, 1L);
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQueryWithArguments() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeQuery(new Object[] { 1L });
		verify(stmt, times(1)).setObject(1, 1L);
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldCallCommit() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeUpdate().get().commit();
		verify(conn, times(1)).commit();
	}

	@Test
	public void shouldCallRollback() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplateDriver.create(conn).prepareStatement(stmtSql).get().executeUpdate().get().rollback();
		verify(conn, times(1)).rollback();
	}
}

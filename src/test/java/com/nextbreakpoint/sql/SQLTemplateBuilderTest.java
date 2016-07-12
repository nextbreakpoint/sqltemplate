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
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SQLTemplateBuilderTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void shouldReturnNotNull() {
		Connection conn = mock(Connection.class);
		assertNotNull(SQLTemplateBuilder.create().build());
	}

	@Test
	public void shouldCallSetAutoCommitWithTrue() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplateBuilder.create().autoCommit().build().apply(conn).get();
		verify(conn, times(1)).setAutoCommit(true);
	}

	@Test
	public void shouldCallSetAutoCommitWithFalse() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplateBuilder.create().noAutoCommit().build().apply(conn).get();
		verify(conn, times(1)).setAutoCommit(false);
	}

	@Test
	public void shouldCallCommit() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplateBuilder.create().commit().build().apply(conn).get();
		verify(conn, times(1)).commit();
	}

	@Test
	public void shouldCallRollback() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplateBuilder.create().rollback().build().apply(conn).get();
		verify(conn, times(1)).rollback();
	}

	@Test
	public void shouldCallPrepareStatement() throws SQLException {
		Connection conn = mock(Connection.class);
		ResultSet rs = mock(ResultSet.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("SELECT * FROM TEST")).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		SQLTemplateBuilder.create().statement("SELECT * FROM TEST").build().apply(conn).get();
		verify(conn, times(1)).prepareStatement("SELECT * FROM TEST");
	}

	@Test
	public void shouldCallExecuteUpdate() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLTemplateBuilder.create().statement("XXX").update().build().apply(conn).get();
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQuery() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		SQLTemplateBuilder.create().statement("XXX").query().build().apply(conn).get();
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldCallExecuteUpdateWithParameters() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplateBuilder.create().statement("XXX").update(new String[] {"X", "Y"}).build().apply(conn).get();
		verify(stmt, times(1)).setObject(1, "X");
		verify(stmt, times(1)).setObject(2, "Y");
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQueryWithParameters() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		SQLTemplateBuilder.create().statement("XXX").query(new String[] {"X", "Y"}).build().apply(conn).get();
		verify(stmt, times(1)).setObject(1, "X");
		verify(stmt, times(1)).setObject(2, "Y");
		verify(stmt, times(1)).executeQuery();
	}
}

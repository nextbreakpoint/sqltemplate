/*
 * This file is part of SQLDriver
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLBuilderTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void shouldReturnNotNull() {
		Connection conn = mock(Connection.class);
		assertNotNull(SQLBuilder.create().build());
	}

	@Test
	public void shouldCallSetAutoCommitWithTrue() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLBuilder.create().autoCommit().build().apply(conn);
		verify(conn, times(1)).setAutoCommit(true);
	}

	@Test
	public void shouldCallSetAutoCommitWithFalse() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLBuilder.create().noAutoCommit().build().apply(conn);
		verify(conn, times(1)).setAutoCommit(false);
	}

	@Test
	public void shouldCallCommit() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLBuilder.create().commit().build().apply(conn);
		verify(conn, times(1)).commit();
	}

	@Test
	public void shouldCallRollback() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLBuilder.create().rollback().build().apply(conn);
		verify(conn, times(1)).rollback();
	}

	@Test
	public void shouldCallPrepareStatement() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLBuilder.create().prepareStatement("SELECT * FROM TEST").build().apply(conn);
		verify(conn, times(1)).prepareStatement("SELECT * FROM TEST");
	}

	@Test
	public void shouldCallExecuteUpdate() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLBuilder.create().prepareStatement("XXX").executeUpdate().build().apply(conn);
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQuery() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLBuilder.create().prepareStatement("XXX").executeQuery().build().apply(conn);
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldCallExecuteUpdateWithParameters() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLBuilder.create().prepareStatement("XXX").executeUpdate(new String[] {"X", "Y"}).build().apply(conn);
		verify(stmt, times(1)).setObject(1, "X");
		verify(stmt, times(1)).setObject(2, "Y");
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQueryWithParameters() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLBuilder.create().prepareStatement("XXX").executeQuery(new String[] {"X", "Y"}).build().apply(conn);
		verify(stmt, times(1)).setObject(1, "X");
		verify(stmt, times(1)).setObject(2, "Y");
		verify(stmt, times(1)).executeQuery();
	}
}

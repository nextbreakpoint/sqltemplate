/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SQLTemplateTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldNotReturnNull() {
		assertNotNull(SQLTemplate.builder().build());
	}

	@Test
	public void shouldCallSetAutoCommitWithTrue() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplate.builder().autoCommit().build().apply(conn);
		verify(conn, times(1)).setAutoCommit(true);
	}

	@Test
	public void shouldCallSetAutoCommitWithFalse() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplate.builder().noAutoCommit().build().apply(conn);
		verify(conn, times(1)).setAutoCommit(false);
	}

	@Test
	public void shouldCallCommit() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplate.builder().commit().build().apply(conn);
		verify(conn, times(1)).commit();
	}

	@Test
	public void shouldCallRollback() throws SQLException {
		Connection conn = mock(Connection.class);
		SQLTemplate.builder().rollback().build().apply(conn);
		verify(conn, times(1)).rollback();
	}

	@Test
	public void shouldCallPrepareStatement() throws SQLException {
		Connection conn = mock(Connection.class);
		ResultSet rs = mock(ResultSet.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("SELECT * FROM TEST")).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		SQLTemplate.builder().statement("SELECT * FROM TEST").build().apply(conn);
		verify(conn, times(1)).prepareStatement("SELECT * FROM TEST");
	}

	@Test
	public void shouldCallExecuteUpdate() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		SQLTemplate.builder().statement("XXX").update().build().apply(conn);
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void shouldCallExecuteQuery() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		SQLTemplate.builder().statement("XXX").query().build().apply(conn);
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldCallExecuteUpdateWithParameters() throws SQLException {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		when(conn.prepareStatement("XXX")).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplate.builder().statement("XXX").update(new String[] {"X", "Y"}).build().apply(conn);
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
		SQLTemplate.builder().statement("XXX").query(new String[] {"X", "Y"}).build().apply(conn);
		verify(stmt, times(1)).setObject(1, "X");
		verify(stmt, times(1)).setObject(2, "Y");
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void shouldReturnEmptyListWhenExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().update().build().apply(conn);
		assertTrue(template.isFailure());
		assertFalse(template.isPresent());
	}

	@Test
	public void shouldReturnEmptyListWhenExecuteQuery() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().query().build().apply(conn);
		assertTrue(template.isFailure());
		assertFalse(template.isPresent());
	}

	@Test
	public void shouldReturnNotEmptyListWhenExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> result = SQLTemplate.builder().statement(stmtSql).update().build().apply(conn);
		assertFalse(result.isFailure());
		assertNotNull(result.get());
		assertEquals(10L, result.get().get(0)[0]);
	}

	@Test
	public void shouldReturnNotEmptyListWhenExecuteQuery() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		ResultSet rs = mock(ResultSet.class);
		ResultSetMetaData meta = mock(ResultSetMetaData.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeQuery()).thenReturn(rs);
		doNothing().when(rs).close();
		when(rs.next()).thenReturn(true, true, false);
		when(meta.getColumnCount()).thenReturn(2);
		when(rs.getMetaData()).thenReturn(meta);
		when(rs.getObject(1)).thenReturn(1L);
		when(rs.getObject(2)).thenReturn("a");
		Try<List<Object[]>, SQLTemplateException> result = SQLTemplate.builder().statement(stmtSql).query().build().apply(conn);
		assertFalse(result.isFailure());
		Object[] findFirst = result.get().get(0);
		assertEquals(1L, findFirst[0]);
		assertEquals("a", findFirst[1]);
	}

	@Test
	public void shouldReturnFailureWhenSetAutoCommitFalseThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(false);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().noAutoCommit().build().apply(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenSetAutoCommitTrueThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(true);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().autoCommit().build().apply(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenCommitThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).commit();
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().commit().build().apply(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenRollbackThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).rollback();
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().rollback().build().apply(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenPrepareStatementThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		doThrow(SQLException.class).when(stmt).setObject(any(Integer.class), any(Object.class));
		String stmtSql = "select * from test where id=?";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().statement(stmtSql).query(new String[] { "X" }).build().apply(conn);
		assertTrue(template.isFailure());
	}
}

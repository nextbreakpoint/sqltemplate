/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.nextbreakpoint.Try;

public class SQLTemplateTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldReturnNotNull() {
		Connection conn = mock(Connection.class);
		assertNotNull(SQLTemplate.builder());
	}

	@Test
	public void shouldReturnEmptyListWhenExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().executeUpdate().build().run(conn);
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
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().executeQuery().build().run(conn);
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
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().prepareStatement(stmtSql).executeUpdate().build().run(conn);
		assertFalse(template.isFailure());
		assertNotNull(template.get());
		assertEquals(10L, template.get().get(0)[0]);
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
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().prepareStatement(stmtSql).executeQuery().build().run(conn);
		assertFalse(template.isFailure());
		assertNotNull(template.get());
		Object[] findFirst = template.get().get(0);
		assertEquals(1L, findFirst[0]);
		assertEquals("a", findFirst[1]);
	}

	@Test
	public void shouldReturnFailureWhenSetAutoCommitFalseThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(false);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().noAutoCommit().build().run(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenSetAutoCommitTrueThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(true);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().autoCommit().build().run(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenSetCommitThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).commit();
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().commit().build().run(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenSetRollbackThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).rollback();
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().rollback().build().run(conn);
		assertTrue(template.isFailure());
	}

	@Test
	public void shouldReturnFailureWhenPrepareStatementThrowsException() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		doThrow(SQLException.class).when(stmt).setObject(any(Integer.class), any(Object.class));
		String stmtSql = "select * from test where id=?";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		Try<List<Object[]>, SQLTemplateException> template = SQLTemplate.builder().prepareStatement(stmtSql).executeQuery(new String[] { "X" }).build().run(conn);
		assertTrue(template.isFailure());
	}
}

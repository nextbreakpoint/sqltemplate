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
import static org.mockito.Mockito.times;
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
	public void create_givenConnectionIsNull_shouldThrowException() {
		exception.expect(NullPointerException.class);
		SQLTemplate.of(null);
	}

	@Test
	public void create_givenConnectionIsNotNull_shouldReturnSQL() {
		Connection conn = mock(Connection.class);
		assertNotNull(SQLTemplate.of(conn));
	}

	@Test
	public void execute_givenCommandIsNull_shouldThrowException() {
		exception.expect(NullPointerException.class);
		Connection conn = mock(Connection.class);
		SQLTemplate.of(conn).execute((SQLCommand)null);
	}

	@Test
	public void execute_givenCommandIsNotEmpty_shouldCallCommand() {
		Connection conn = mock(Connection.class);
		SQLCommand cmd = mock(SQLCommand.class);
		when(cmd.apply(any(SQLDriver.class))).thenReturn(SQLDriver.success(SQLDriver.create(conn)));
		SQLTemplate.of(conn).execute(SQLCommand.begin(cmd));
		verify(cmd, times(1)).apply(any(SQLDriver.class));
	}

	@Test
	public void execute_givenCommandIsNoAutoCommit_shouldSetAutoCommitToFalse() throws Exception {
		Connection conn = mock(Connection.class);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.noAutoCommit()));
		verify(conn, times(1)).setAutoCommit(false);
	}

	@Test
	public void execute_givenCommandIsAutoCommit_shouldSetAutoCommitToTrue() throws Exception {
		Connection conn = mock(Connection.class);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.autoCommit()));
		verify(conn, times(1)).setAutoCommit(true);
	}

	@Test
	public void execute_givenCommandIsPrepareStatement_shouldCallPrepareStatement() throws Exception {
		Connection conn = mock(Connection.class);
		String stmtSql = "select * from test";
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)));
		verify(conn, times(1)).prepareStatement(stmtSql);
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecute_shouldCallExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "delete from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.execute()));
		verify(stmt, times(1)).executeUpdate();
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecuteQuery_shouldCallExecuteUpdate() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.executeQuery()));
		verify(stmt, times(1)).executeQuery();
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecuteAndThenCommit_shouldCallCommit() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.execute()).andThen(sql -> sql.commit()));
		verify(conn, times(1)).commit();
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecuteAndThenRollback_shouldCallRollback() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(1);
		SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.execute()).andThen(sql -> sql.rollback()));
		verify(conn, times(1)).rollback();
	}

	@Test
	public void execute_givenCommandReturnsFailure_shouldNotHaveResult() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> SQLDriver.failure(new Exception()))).flatMap(sql -> SQLDriver.success(sql.get()));
		assertTrue(tryStream.isFailure());
		assertTrue(!tryStream.isPresent());
	}

	@Test
	public void execute_givenCommandReturnsSuccess_shouldReturnEmptyStream() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> SQLDriver.success(sql))).flatMap(sql -> SQLDriver.success(sql.get()));
		assertFalse(tryStream.isFailure());
		assertNotNull(tryStream.get());
		assertEquals(0L, tryStream.get().size());
	}

	@Test
	public void execute_givenCommandIsExecute_shouldReturnEmptyStream() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.execute())).flatMap(sql -> SQLDriver.success(sql.get()));
		assertTrue(tryStream.isFailure());
		assertFalse(tryStream.isPresent());
	}

	@Test
	public void execute_givenCommandIsExecuteQuery_shouldReturnEmptyStream() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.executeQuery())).flatMap(sql -> SQLDriver.success(sql.get()));
		assertTrue(tryStream.isFailure());
		assertFalse(tryStream.isPresent());
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecute_shouldReturnStream() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		String stmtSql = "select * from test";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		when(stmt.executeUpdate()).thenReturn(10);
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.execute())).flatMap(sql -> SQLDriver.success(sql.get()));
		assertFalse(tryStream.isFailure());
		assertNotNull(tryStream.get());
		assertEquals(10L, tryStream.get().get(0)[0]);
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecuteQuery_shouldReturnStream() throws Exception {
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
		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.executeQuery())).flatMap(sql -> SQLDriver.success(sql.get()));
		assertFalse(tryStream.isFailure());
		assertNotNull(tryStream.get());
		Object[] findFirst = tryStream.get().get(0);
		assertEquals(1L, findFirst[0]);
		assertEquals("a", findFirst[1]);
	}

//	@Test
//	public void execute_givenCommandIsPrepareStatementAndThenExecuteQueryAndThenPeek_shouldCaptureResult() throws Exception {
//		@SuppressWarnings("unchecked")
//		Consumer<Object[]> consumer = mock(Consumer.class);
//		Connection conn = mock(Connection.class);
//		PreparedStatement stmt = mock(PreparedStatement.class);
//		ResultSet rs = mock(ResultSet.class);
//		ResultSetMetaData meta = mock(ResultSetMetaData.class);
//		String stmtSql = "select * from test";
//		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
//		when(stmt.executeQuery()).thenReturn(rs);
//		doNothing().when(rs).close();
//		when(rs.next()).thenReturn(true);
//		when(meta.getColumnCount()).thenReturn(2);
//		when(rs.getMetaData()).thenReturn(meta);
//		when(rs.getObject(1)).thenReturn(1L);
//		when(rs.getObject(2)).thenReturn("a");
//		Try<List<Object[]>, SQLTemplateException> tryStream = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.executeQuery())).flatMap(sql -> SQLDriver.success(sql.get()).peek(consumer));
//		assertFalse(tryStream.isFailure());
//		assertNotNull(tryStream.get());
//		verify(consumer, times(1)).accept(any(SQLDriver.class));
//	}

	@Test
	public void execute_givenCommandThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> SQLDriver.of(() -> { throw new Exception(); })));
		assertTrue(trySQL.isFailure());
	}

	@Test
	public void execute_givenCommandIsNoAutoCommitAndConnectionThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(false);
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.noAutoCommit()));
		assertTrue(trySQL.isFailure());
	}

	@Test
	public void execute_givenCommandIsAutoCommitAndConnectionThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).setAutoCommit(true);
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.autoCommit()));
		assertTrue(trySQL.isFailure());
	}

	@Test
	public void execute_givenCommandIsCommitAndConnectionThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).commit();
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.commit()));
		assertTrue(trySQL.isFailure());
	}

	@Test
	public void execute_givenCommandIsRollbackAndConnectionThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		doThrow(SQLException.class).when(conn).rollback();
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.rollback()));
		assertTrue(trySQL.isFailure());
	}

	@Test
	public void execute_givenCommandIsPrepareStatementAndThenExecuteQueryAndConnectionThrowsException_shouldReturnFailure() throws Exception {
		Connection conn = mock(Connection.class);
		PreparedStatement stmt = mock(PreparedStatement.class);
		doThrow(SQLException.class).when(stmt).setObject(any(Integer.class), any(Object.class));
		String stmtSql = "select * from test where id=?";
		when(conn.prepareStatement(stmtSql)).thenReturn(stmt);
		Try<SQLTemplate, SQLTemplateException> trySQL = SQLTemplate.of(conn).execute(SQLCommand.begin(sql -> sql.prepareStatement(stmtSql)).andThen(sql -> sql.executeQuery(new String[] { "X" })));
		assertTrue(trySQL.isFailure());
	}
}

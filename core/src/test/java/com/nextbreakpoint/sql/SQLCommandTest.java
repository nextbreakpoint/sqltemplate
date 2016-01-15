/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SQLCommandTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void begin_shouldReturnACommand() {
		assertTrue(SQLCommand.begin() != null);
	}

	@Test
	public void apply_givenSQLTemplate_shouldReturnSameObject() {
		SQLTemplate sql = mock(SQLTemplate.class);
		assertEquals(sql, SQLCommand.begin().apply(sql).get());
	}

	@Test
	public void apply_givenCommandIsAutoCommit_shouldCallAutoCommit() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().autoCommit().apply(sql);
		verify(sql, times(1)).autoCommit();
	}

	@Test
	public void apply_givenCommandIsNoAutoCommit_shouldCallAutoCommit() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().noAutoCommit().apply(sql);
		verify(sql, times(1)).noAutoCommit();
	}

	@Test
	public void apply_givenCommandIsCommit_shouldCallCommit() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().commit().apply(sql);
		verify(sql, times(1)).commit();
	}

	@Test
	public void apply_givenCommandIsRollback_shouldCallRollback() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().rollback().apply(sql);
		verify(sql, times(1)).rollback();
	}

	@Test
	public void apply_givenCommandIsPrepareStatement_shouldCallPrepareStatement() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().prepareStatement("SELECT * FROM TEST").apply(sql);
		verify(sql, times(1)).prepareStatement("SELECT * FROM TEST");
	}

	@Test
	public void apply_givenCommandIsExecute_shouldCallExecute() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().execute().apply(sql);
		verify(sql, times(1)).execute();
	}

	@Test
	public void apply_givenCommandIsExecuteQuery_shouldCallExecuteQuery() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().executeQuery().apply(sql);
		verify(sql, times(1)).executeQuery();
	}

	@Test
	public void apply_givenCommandIsExecuteWithParams_shouldCallExecute() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().execute(new String[] {"X", "Y"}).apply(sql);
		verify(sql, times(1)).execute(new String[] {"X", "Y"});
	}

	@Test
	public void apply_givenCommandIsExecuteQueryWithParams_shouldCallExecuteQuery() {
		SQLTemplate sql = mock(SQLTemplate.class);
		SQLCommand.begin().executeQuery(new String[] {"X", "Y"}).apply(sql);
		verify(sql, times(1)).executeQuery(new String[] {"X", "Y"});
	}

	@Test
	public void apply_givenCommandIsPeek_shouldCallConsumer() {
		SQLTemplate sql = mock(SQLTemplate.class);
		@SuppressWarnings("unchecked")
		Consumer<SQLTemplate> consumer = mock(Consumer.class);
		SQLCommand.begin().peek(consumer).apply(sql);
		verify(consumer, times(1)).accept(sql);
	}
}

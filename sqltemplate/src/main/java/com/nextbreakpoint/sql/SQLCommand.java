/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.util.function.Consumer;

import com.nextbreakpoint.Try;

@FunctionalInterface
public interface SQLCommand {
	public Try<SQLTemplate, SQLTemplateException> apply(SQLTemplate sql);

	public static SQLCommand begin() {
		return sql -> Try.success(SQLTemplate.wrapException(), sql);
	}

	public static SQLCommand begin(SQLCommand cmd) {
		return sql -> cmd.apply(sql);
	}

	public default SQLCommand andThen(SQLCommand cmd) {
		return sql1 -> apply(sql1).flatMap(sql2 -> cmd.apply(sql2));
	}

	public default SQLCommand autoCommit() {
		return this.andThen(sql -> sql.autoCommit());
	}

	public default SQLCommand noAutoCommit() {
		return this.andThen(sql -> sql.noAutoCommit());
	}
	
	public default SQLCommand commit() {
		return this.andThen(sql -> sql.commit());
	}
	
	public default SQLCommand rollback() {
		return this.andThen(sql -> sql.rollback());
	}

	public default SQLCommand prepareStatement(String sqlStmt) {
		return this.andThen(sql -> sql.prepareStatement(sqlStmt));
	}
	
	public default SQLCommand execute(Object[] params) {
		return this.andThen(sql -> sql.execute(params));
	}

	public default SQLCommand executeQuery(Object[] params) {
		return this.andThen(sql -> sql.executeQuery(params));
	}

	public default SQLCommand execute() {
		return this.andThen(sql -> sql.execute());
	}

	public default SQLCommand executeQuery() {
		return this.andThen(sql -> sql.executeQuery());
	}

	public default SQLCommand peek(Consumer<SQLTemplate> consumer) {
		return state -> {
			Try<SQLTemplate, SQLTemplateException> result = apply(state);
			result.ifPresent(consumer);
			return result; 
		};
	}
}

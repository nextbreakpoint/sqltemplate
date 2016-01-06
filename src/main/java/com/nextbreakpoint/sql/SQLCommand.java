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

	public default SQLCommand andThen(SQLCommand cmd) {
		return sql1 -> apply(sql1).flatMap(sql2 -> cmd.apply(sql2));
	}

	public default SQLCommand peek(Consumer<SQLTemplate> consumer) {
		return state -> {
			Try<SQLTemplate, SQLTemplateException> result = apply(state);
			result.ifPresent(consumer);
			return result; 
		};
	}

	public static SQLCommand begin(SQLCommand cmd) {
		return sql -> cmd.apply(sql);
	}

	public static SQLCommand create(SQLCommand cmd) {
		return cmd;
	}
}

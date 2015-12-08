package com.nextbreakpoint.sql;

import java.util.function.Consumer;

import com.nextbreakpoint.Try;

@FunctionalInterface
public interface SQLCommand {
	public Try<SQLTemplate> apply(SQLTemplate sql);

	public default SQLCommand andThen(SQLCommand cmd) {
		return sql1 -> apply(sql1).flatMap(sql2 -> cmd.apply(sql2));
	}

	public default SQLCommand peek(Consumer<SQLTemplate> consumer) {
		return state -> {
			Try<SQLTemplate> result = apply(state);
			result.ifPresent(consumer);
			return result; 
		};
	}

	public static SQLCommand empty() {
		return sql -> Try.success(sql);
	}

	public static SQLCommand create(SQLCommand cmd) {
		return cmd;
	}
}

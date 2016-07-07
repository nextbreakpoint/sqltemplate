/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public class SQLTemplateMain {
	private static Object run() throws Exception {
		execute(connection -> template().apply(connection).get()
			.stream().map(columns -> columns[1]).forEach(System.out::println));
		return null;
	}

	private static SQLTemplate template() {
		return SQLTemplate.builder()
            .noAutoCommit()
            .statement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))")
            .update()
            .statement("DELETE TEST")
            .update()
            .commit()
            .statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
            .update(new Object[] { 1, "A" })
            .update(new Object[] { 2, "B" })
            .commit()
            .update(new Object[] { 3, "C" })
            .update(new Object[] { 4, "D" })
            .rollback()
            .statement("SELECT * FROM TEST")
            .query()
            .build();
	}

	private static void execute(Consumer<Connection> consumer) throws SQLException {
		try (Connection connection = getConnection()) { consumer.accept(connection); } finally {}
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
	}

	private static Class<?> loadDriver() throws ClassNotFoundException {
		return Class.forName("org.h2.Driver");
	}

	private static Consumer<Throwable> exceptionHandler() {
		return e -> e.printStackTrace();
	}

	public static void main(String[] args) {
		Try.of(SQLTemplateMain::loadDriver).flatMap(clazz -> Try.of(SQLTemplateMain::run)).onFailure(exceptionHandler());
	}
}

/*
 * This file is part of SQLTemplate
 * 
 * Copyright (c) 2016, Andrea Medeghini
 * All rights reserved.
 */
package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLTemplateMain {
	public static void main(String[] args) {
		try {
			Class.forName("org.h2.Driver");
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void run() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")) {
			SQLCommand cmd = SQLCommand.begin()
				.noAutoCommit() 
				.prepareStatement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')") 
				.execute() 
				.prepareStatement("DELETE TEST") 
				.execute() 
				.commit()
				.prepareStatement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
				.execute(new Object[] { 1, "A" }) 
				.execute(new Object[] { 2, "B" })
				.commit()
				.execute(new Object[] { 3, "C" })
				.execute(new Object[] { 4, "D" })
				.rollback()
				.prepareStatement("SELECT * FROM TEST")
				.executeQuery();

			SQLTemplate.with(conn).execute(cmd).get().stream().map(columns -> columns[1]).forEach(System.out::println);
		} finally {
		}
	}
}

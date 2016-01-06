package com.nextbreakpoint.sql;

import java.sql.Connection;
import java.sql.DriverManager;

import com.nextbreakpoint.sql.SQLCommand;
import com.nextbreakpoint.sql.SQLTemplate;

public class SQLTemplateMain {
	public static void main(String[] args) {
		try {
			Class.forName("org.h2.Driver");
			run();
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void run() throws Exception {
		try (Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")) {
			SQLCommand cmd = SQLCommand.begin(sql -> sql.noAutoCommit()) 
				.andThen(sql -> sql.prepareStatement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")) 
				.andThen(sql -> sql.execute()) 
				.andThen(sql -> sql.prepareStatement("DELETE TEST")) 
				.andThen(sql -> sql.execute()) 
				.peek(sql -> sql.stream().map(columns -> columns[0]).forEach(System.out::println))
				.andThen(sql -> sql.prepareStatement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")) 
				.andThen(sql -> sql.execute(new Object[] { 1, "A" })) 
				.andThen(sql -> sql.execute(new Object[] { 2, "B" })) 
				.andThen(sql -> sql.commit()) 
				.andThen(sql -> sql.execute(new Object[] { 3, "C" })) 
				.andThen(sql -> sql.execute(new Object[] { 4, "D" })) 
				.andThen(sql -> sql.rollback()) 
				.andThen(sql -> sql.prepareStatement("SELECT * FROM TEST")) 
				.andThen(sql -> sql.executeQuery()) 
				.peek(sql -> sql.stream().map(columns -> columns[1]).forEach(System.out::println)); 
			
			SQLTemplate.create(conn).execute(cmd).ifPresentOrThrow(something -> { System.out.println("done"); });
		} finally {
		}
	}
}

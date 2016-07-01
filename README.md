# SQLTemplate 1.3.0

SQLTemplate implements a fluent API for executing SQL statements

## Example

Given the program:

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
                SQLTemplate.builder()
                    .noAutoCommit()
                    .statement("CREATE TABLE IF NOT EXISTS TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
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
                    .build()
                    .apply(conn)
                    .get().stream().map(columns -> columns[1]).forEach(System.out::println);
            } finally {
            }
        }
    }
		
The output will be:

	A
	B
	done

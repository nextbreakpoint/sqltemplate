# SQLTemplate 1.3.1

SQLTemplate implements a fluent API for executing SQL statements

## Example

Given the program:

    public class SQLTemplateMain {
        private static Object run() throws Exception {
            execute(connection -> {
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
                    .apply(connection).get().stream().map(columns -> columns[1]).forEach(System.out::println);
            });
            return null;
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
		
The output will be:

	A
	B

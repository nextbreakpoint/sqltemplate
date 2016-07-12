# SQLTemplate 1.4.0

SQLTemplate implements a fluent API for executing SQL statements

## Easy SQL statements

SQLTemplate has been designed for executing SQL statements in tests removing the complexity of handling JDBC exceptions.

Statements are represented as a chain of operations which are applied to a JDBC connection.

Tipical use cases are creating tables, inserting data or quering data:

    SQLTemplate.builder().autoCommit().statement("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255) DEFAULT '')")
        .update().build().apply(connection).get();
    
    SQLTemplate.builder().autoCommit().statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
        .update(new Object[] { 1, "A" }).build().apply(connection).get();

    SQLTemplate.builder().autoCommit().statement("SELECT NAME FROM TEST")
        .query().build().apply(connection).get().stream().forEach(System.out::println));

## Other examples

Given the program:

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
    
        private static Consumer<Exception> exceptionHandler() {
            return e -> e.printStackTrace();
        }
    
        public static void main(String[] args) {
            Try.of(SQLTemplateMain::loadDriver).flatMap(clazz -> Try.of(SQLTemplateMain::run)).ifFailure(exceptionHandler());
        }
    }
		
The output will be:

	A
	B

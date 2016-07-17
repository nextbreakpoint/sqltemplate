# SQLTemplate 1.5.0

SQLTemplate implements a fluent interface for executing SQL statements.

SQLTemplate has been designed to simplify the execution of SQL statements in tests or any application which doesn't require complex SQL operations.

## Getting binaries

SQLTemplate is available in Maven Central Repository, Bintray and GitHub. 

If you are using Maven, add a dependency in your POM:

    <dependency>
        <groupId>com.nextbreakpoint</groupId>
        <artifactId>com.nextbreakpoint.sqltemplate</artifactId>
        <version>1.5.0</version>
    </dependency>

If you are using other tools, please consult tool's documentation.
  
## License

SQLTemplate is distributed under the terms of BSD 3-Clause License.

    Copyright (c) 2016, Andrea Medeghini
    All rights reserved.
    
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    
    * Redistributions of source code must retain the above copyright notice, this
      list of conditions and the following disclaimer.
    
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    
    * Neither the name of SQLTemplate nor the names of its
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.
    
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
    DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
    CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  
## Basic examples

Tipical use cases are creating tables, inserting data or quering data:

    SQLTemplate.builder().autoCommit().statement("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))")
        .update().build().apply(connection);
    
    SQLTemplate.builder().autoCommit().statement("INSERT INTO TEST (ID, NAME) VALUES (?, ?)")
        .update(new Object[] { 1, "A" }).build().apply(connection);

    SQLTemplate.builder().autoCommit().statement("SELECT NAME FROM TEST")
        .query().build().apply(connection).get().stream().forEach(System.out::println));

## Complete example

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

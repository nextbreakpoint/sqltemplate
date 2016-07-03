package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

/**
 * Provides an interface for encapsulating SQL operations.
 *
 * @author Andrea Medeghini
 *
 */
@FunctionalInterface
public interface SQLOperation {
    /**
     * Executes operation using given driver and returns the result as Try instance.
     * @param driver the driver
     * @return the result
     */
    public Try<SQLTemplateDriver, SQLTemplateException> apply(SQLTemplateDriver driver);

    /**
     * Creates a new operation which concatenates the operation with another operation.
     * @param other the other operation
     * @return the new operation
     */
    public default SQLOperation andThen(SQLOperation other) {
        return driver -> this.apply(driver).flatMap(otherDriver -> other.apply(otherDriver));
    }
}

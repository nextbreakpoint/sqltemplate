package com.nextbreakpoint.sql;

import com.nextbreakpoint.Try;

@FunctionalInterface
public interface SQLOperation {
    public Try<SQLTemplateDriver, SQLTemplateException> apply(SQLTemplateDriver driver);

    public default SQLOperation andThen(SQLOperation other) {
        return driver -> this.apply(driver).flatMap(otherDriver -> other.apply(otherDriver));
    }
}

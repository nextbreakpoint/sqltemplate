package com.nextbreakpoint.sql;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SQLTemplateExceptionTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void constructor_givenNullMessage_shouldThrowException() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null);
	}

	@Test
	public void constructor_givenNotNullMessage_shouldNotThrowException() {
		new SQLTemplateException("TEST");
	}
	
	@Test
	public void constructor_givenNullMessageAndNullCause_shouldThrowException() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null, null);
	}
	
	@Test
	public void constructor_givenNotNullMessageAndNullCause_shouldThrowException() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException("TEST", null);
	}

	@Test
	public void constructor_givenNullMessageAndNotNullCause_shouldThrowException() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null, new Exception());
	}

	@Test
	public void constructor_givenNotNullMessageAndNullCause_shouldNotThrowException() {
		new SQLTemplateException("TEST", new Exception());
	}
}

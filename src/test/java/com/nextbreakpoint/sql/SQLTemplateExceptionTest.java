package com.nextbreakpoint.sql;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SQLTemplateExceptionTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void shouldThrowExceptionWhenMessageIsNull() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null);
	}

	@Test
	public void shouldNotThrowExceptionWhenMessageIsNotNull() {
		new SQLTemplateException("TEST");
	}
	
	@Test
	public void shouldThrowExceptionWhenOnlyCauseIsNull() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException("TEST", null);
	}

	@Test
	public void shouldThrowExceptionWhenOnlyMessageIsNull() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null, new Exception());
	}

	@Test
	public void shouldThrowExceptionWhenMessageAndCauseAreNull() {
		exception.expect(NullPointerException.class);
		new SQLTemplateException(null, null);
	}

	@Test
	public void shouldNotThrowExceptionWhenMessageAndCauseAreNotNull() {
		new SQLTemplateException("TEST", new Exception());
	}
}

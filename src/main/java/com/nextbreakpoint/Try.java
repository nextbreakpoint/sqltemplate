package com.nextbreakpoint;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Try<V> {
	private Try() {}

	public abstract Boolean isSuccess();

	public abstract Boolean isFailure();

	public abstract void throwException();
	
	public abstract void ifPresent(Consumer<V> c);

	public abstract void ifPresentOrThrow(Consumer<V> c);

	public abstract <R> Try<R> map(Function<V, R> func);

	public abstract <R> Try<R> flatMap(Function<V, Try<R>> func);

	public abstract boolean isPresent();

	public abstract V get();

	public static <V> Try<V> empty() {
		return Try.success(null);
	}

	public static <V> Try<V> ofNullable(V value) {
		return Try.success(value);
	}

	public static <V> Try<V> failure(String message) {
		return new Failure<>(message);
	}

	public static <V> Try<V> failure(String message, Exception e) {
		return new Failure<>(message, e);
	}

	public static <V> Try<V> failure(Exception e) {
		return new Failure<>(e);
	}

	public static <V> Try<V> success(V value) {
		return new Success<>(value);
	}

	private static class Failure<V> extends Try<V> {
		private RuntimeException exception;

		public Failure(String message) {
			this.exception = new IllegalStateException(message);
		}

		public Failure(String message, Exception e) {
			this.exception = (e instanceof RuntimeException) ? (RuntimeException)e : new IllegalStateException(message, e);
		}
		
		public Failure(Exception e) {
			this.exception = (e instanceof RuntimeException) ? (RuntimeException)e : new IllegalStateException(e);
		}

		@Override
		public Boolean isSuccess() {
			return false;
		}

		@Override
		public Boolean isFailure() {
			return true;
		}

		@Override
		public void throwException() {
			throw this.exception;
		}

		public <R> Try<R> map(Function<V, R> func) {
			return Try.failure(exception);
		}

		public <R> Try<R> flatMap(Function<V, Try<R>> func) {
			return Try.failure(exception);
		}

		public void ifPresent(Consumer<V> c) {
		}

		public void ifPresentOrThrow(Consumer<V> c) {
			throw exception;
		}
		
		public boolean isPresent() {
			return false;
		}

		public V get() {
			throw new NoSuchElementException();
		}
	}

	private static class Success<V> extends Try<V> {
		private V value;

		public Success(V value) {
			this.value = value;
		}

		@Override
		public Boolean isSuccess() {
			return true;
		}

		@Override
		public Boolean isFailure() {
			return false;
		}

		@Override
		public void throwException() {
		}
		
		public <R> Try<R> map(Function<V, R> func) {
			try {
				return Try.ofNullable(func.apply(value));
			} catch (Exception e) {
				return Try.failure(e);
			}
		}

		public <R> Try<R> flatMap(Function<V, Try<R>> func) {
			try {
				if (value != null) {
					return func.apply(value);
				} else {
					return Try.empty();
				}
			} catch (Exception e) {
				return Try.failure(e);
			}
		}

		public void ifPresent(Consumer<V> c) {
			if (value != null) c.accept(value);
		}

		public void ifPresentOrThrow(Consumer<V> c) {
			if (value != null) c.accept(value);
		}

		public boolean isPresent() {
			return value != null;
		}

		public V get() {
			if (value == null) {
				throw new NoSuchElementException();
			}
			return value;
		}
	}
}
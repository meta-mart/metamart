package org.metamart.service.util;

public class RetryableAssertionError extends Exception {
  public RetryableAssertionError(Throwable cause) {
    super(cause);
  }
}

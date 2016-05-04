package com.trunk.rx.json.exception;

public class MalformedPathException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MalformedPathException(String msg) {
    super(msg);
  }

  public MalformedPathException(String msg, Throwable throwable) {
    super(msg);
    // Using initCause() instead of calling super() because Java 1.5 didn't retrofit IOException
    // with a constructor with Throwable. This was done in Java 1.6
    initCause(throwable);
  }

  public MalformedPathException(Throwable throwable) {
    // Using initCause() instead of calling super() because Java 1.5 didn't retrofit IOException
    // with a constructor with Throwable. This was done in Java 1.6
    initCause(throwable);
  }
}

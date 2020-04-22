package com.github.kong.ignite.spring.boot.exception;

/**
 */
public class IgniteException extends RuntimeException {
    private static final long serialVersionUID = 42L;

    public IgniteException(String msg) {
        super(msg);
    }

    public IgniteException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IgniteException(Throwable cause) {
        super(cause);
    }

}
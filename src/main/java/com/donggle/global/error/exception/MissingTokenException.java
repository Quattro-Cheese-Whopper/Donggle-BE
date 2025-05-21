package com.donggle.global.error.exception;

public class MissingTokenException extends RuntimeException {

    public static final RuntimeException EXCEPTION = new MissingTokenException();

    private MissingTokenException() {
        super("Missing token");
    }
}

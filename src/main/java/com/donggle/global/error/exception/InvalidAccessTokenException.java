package com.donggle.global.error.exception;

public class InvalidAccessTokenException extends RuntimeException {

    public static final RuntimeException EXCEPTION = new InvalidAccessTokenException();

    private InvalidAccessTokenException() {
        super("Invalid access token");
    }
}

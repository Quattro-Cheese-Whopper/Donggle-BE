package com.donggle.global.error.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public static final RuntimeException EXCEPTION = new InvalidRefreshTokenException();

    private InvalidRefreshTokenException() {
        super("Invalid refresh token");
    }
}

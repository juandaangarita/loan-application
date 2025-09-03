package com.onix.security.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials provided.");
    }

    public InvalidCredentialsException(String message) {
        super("Invalid credentials. " + message);
    }
}

package com.onix.security.exception;

public class UnauthorizedClientException extends RuntimeException {
    public UnauthorizedClientException(String email) {
        super("Unauthorized client with email " + email);
    }
}

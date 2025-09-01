package com.onix.model.exception;

public class AuthenticationServiceUnavailableException extends RuntimeException {

    public AuthenticationServiceUnavailableException(String email, String documentNumber) {
        super(String.format("User service unavailable. Client with email %s and documentNumber %s couldn't be validated.",
                email, documentNumber));
    }
}

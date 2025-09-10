package com.onix.model.exception;

public class UnregisteredUserException extends RuntimeException {
    public UnregisteredUserException(String email, String documentNumber) {
        super("Client with email " + email + " and documentNumber " + documentNumber + " is not registered.");
    }
}

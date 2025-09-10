package com.onix.model.exception;

import java.util.UUID;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(UUID id) {
        super("Loan with id " + id + " does not exist.");
    }
}

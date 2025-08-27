package com.onix.usecase.exception;

public class InvalidLoanTypeException extends RuntimeException {
    public InvalidLoanTypeException(Integer loanTypeId) {
        super("Loan type with id " + loanTypeId + " does not exist.");
    }
}

package com.onix.usecase.exception;

import java.math.BigDecimal;

public class InvalidAmountLoanException extends RuntimeException {
    public InvalidAmountLoanException(BigDecimal amount, BigDecimal min, BigDecimal max) {
        super("Invalid amount: " + amount + ". Valid range is between " + min + " and " + max);
    }
}

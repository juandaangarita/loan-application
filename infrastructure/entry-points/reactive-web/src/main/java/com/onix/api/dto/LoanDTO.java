package com.onix.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanDTO (
        UUID loanId,
        BigDecimal amount,
        Integer termInMonths,
        String email,
        String documentNumber,
        String loanStatus,
        String loanType) {
}

package com.onix.api.dto;

import java.math.BigDecimal;

public record CreateLoanDTO(
        BigDecimal amount,
        Integer termMonths,
        String email,
        String documentNumber,
        Integer loanTypeId) {
}

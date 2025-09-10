package com.onix.model.loanapplication.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanPageableDTO(
        UUID loanId,
        BigDecimal amount,
        Integer termMonths,
        String email,
        String userName,
        String loanType,
        BigDecimal interestRate,
        String status,
        Long baseSalary,
        BigDecimal monthlyAmountRequested
) {
}

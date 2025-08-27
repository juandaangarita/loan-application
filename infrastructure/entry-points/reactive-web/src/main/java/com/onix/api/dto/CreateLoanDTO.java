package com.onix.api.dto;

public record CreateLoanDTO(
        Long amount,
        Integer termInMonths,
        String email,
        String documentNumber,
        Integer loanTypeId) {
}

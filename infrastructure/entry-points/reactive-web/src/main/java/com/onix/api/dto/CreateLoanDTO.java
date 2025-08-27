package com.onix.api.dto;

public record CreateLoanDTO(
        Long amount,
        Integer termMonths,
        String email,
        String documentNumber,
        Integer loanTypeId) {
}

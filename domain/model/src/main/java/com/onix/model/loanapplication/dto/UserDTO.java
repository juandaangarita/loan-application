package com.onix.model.loanapplication.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserDTO(
        UUID userId,
        String name,
        String lastname,
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        Long baseSalary
) {
}

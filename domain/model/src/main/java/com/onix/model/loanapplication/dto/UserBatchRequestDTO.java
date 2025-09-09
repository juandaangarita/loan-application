package com.onix.model.loanapplication.dto;

import java.util.Set;

public record UserBatchRequestDTO(
        Set<String> emails
) {
}

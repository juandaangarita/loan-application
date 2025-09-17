package com.onix.api.dto;

import java.util.UUID;

public record UpdateLoanStatusDTO (
        UUID id,
        String status){
}

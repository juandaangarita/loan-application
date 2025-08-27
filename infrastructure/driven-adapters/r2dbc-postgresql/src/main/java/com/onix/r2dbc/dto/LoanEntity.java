package com.onix.r2dbc.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table("loan_application")
public class LoanEntity {
    @Id
    private UUID loanId;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Integer loanTypeId;
    private Integer statusId;
}

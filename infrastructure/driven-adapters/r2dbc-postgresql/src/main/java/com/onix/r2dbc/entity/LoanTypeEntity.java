package com.onix.r2dbc.entity;

import java.math.BigDecimal;
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
@Table("loan_types")
public class LoanTypeEntity {
    @Id
    private Integer loanTypeId;
    private String name;
    private String description;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private Boolean autoValidation;
}

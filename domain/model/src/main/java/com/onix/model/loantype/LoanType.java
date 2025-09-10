package com.onix.model.loantype;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class LoanType {
    private Integer loanTypeId;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
}

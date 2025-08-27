package com.onix.model.loanapplication;
import java.util.UUID;
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
public class Loan {
    private UUID loanId;
    private Long amount;
    private int termMonths;
    private String email;
    private String documentNumber;
}

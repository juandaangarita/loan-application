package com.onix.usecase.loanapplication.validator;

import com.onix.model.loanapplication.Loan;
import reactor.core.publisher.Mono;

public class LoanValidator {

    public Mono<Void> validate(Loan loan) {
        // Implement validation logic here
        return Mono.empty();
    }
}

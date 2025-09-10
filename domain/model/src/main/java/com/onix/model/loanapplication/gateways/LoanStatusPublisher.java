package com.onix.model.loanapplication.gateways;

import com.onix.model.loanapplication.Loan;
import reactor.core.publisher.Mono;

public interface LoanStatusPublisher {
    Mono<Void> sendStatusUpdate(Loan loan, String status);
}

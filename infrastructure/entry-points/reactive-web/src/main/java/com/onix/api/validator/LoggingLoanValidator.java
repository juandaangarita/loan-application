package com.onix.api.validator;

import com.onix.model.loanapplication.Loan;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingLoanValidator {

    private final LoanValidator validator;

    public Mono<Void> validate(Loan loan) {
        log.trace("Validating loan: {}", loan.toString());
        return validator.validate(loan)
                .doOnError(e -> log.trace("Validation failed: {}", e.getMessage()))
                .doOnSuccess(v -> log.trace("Validation succeeded for loan: {}", loan));
    }
}

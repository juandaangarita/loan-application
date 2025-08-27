package com.onix.usecase.loanapplication;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanValidator loanValidator;

    public Mono<Loan> createLoanApplication(Loan loan) {
        return loanValidator.validate(loan)
                .then(loanRepository.saveLoanApplication(loan));
    }
}

package com.onix.model.loanapplication.gateways;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanRepository {
    Mono<Loan> saveLoanApplication(Loan loan);
    Flux<LoanPageableDTO> findPendingLoans(int page, int size, String sortBy, String filter);
}

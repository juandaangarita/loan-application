package com.onix.model.loanapplication.gateways;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.UserDTO;
import java.util.List;
import reactor.core.publisher.Mono;

public interface LoanPublisher {
    Mono<Void> sendStatusUpdate(Loan loan, String status, String username);
    Mono<Void> sendCreationEvent(Loan newloan, List<Loan> approvedLoans, UserDTO user);
}

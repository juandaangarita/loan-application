package com.onix.model.loanstatus.gateways;

import com.onix.model.loanstatus.LoanStatus;
import reactor.core.publisher.Mono;

public interface LoanStatusRepository {
    Mono<LoanStatus> getStatusByName(String name);
}

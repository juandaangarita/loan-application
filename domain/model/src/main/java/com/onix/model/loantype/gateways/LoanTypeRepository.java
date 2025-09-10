package com.onix.model.loantype.gateways;

import com.onix.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> getLoanTypeById(Integer id);
}

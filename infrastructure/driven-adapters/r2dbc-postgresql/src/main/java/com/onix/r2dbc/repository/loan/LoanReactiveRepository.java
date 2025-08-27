package com.onix.r2dbc.repository.loan;

import com.onix.r2dbc.entity.LoanEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, String>, ReactiveQueryByExampleExecutor<LoanEntity> {

}

package com.onix.r2dbc.repository.loanstatus;

import com.onix.model.loanstatus.LoanStatus;
import com.onix.r2dbc.entity.LoanStatusEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanStatusReactiveRepository extends ReactiveCrudRepository<LoanStatusEntity, Integer>, ReactiveQueryByExampleExecutor<LoanStatusEntity> {

    @Query(value = """
            SELECT ls.status_id,
                    ls.name,
                    ls.description
            FROM loan_statuses ls
            WHERE ls.name = :status
            """)
    Mono<LoanStatus> findByName(String status);

}

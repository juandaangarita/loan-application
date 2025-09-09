package com.onix.r2dbc.repository.loan;

import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.r2dbc.entity.LoanEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface LoanReactiveRepository extends ReactiveCrudRepository<LoanEntity, String>, ReactiveQueryByExampleExecutor<LoanEntity> {

    @Query(value = """
            SELECT la.loan_id,
                    la.amount,
                    la.term_months,
                    la.email,
                    lt.name AS loan_type,
                    lt.interest_rate,
                    ls.name AS status,
                    (la.amount / la.term_months) AS monthly_amount_requested
            FROM loan_application la
            INNER JOIN loan_types lt ON la.loan_type_id = lt.loan_type_id
            INNER JOIN loan_statuses ls ON la.status_id = ls.status_id
            WHERE ls.name = 'Pending Review'
            LIMIT :#{#pageable.pageSize}
            OFFSET :#{#pageable.offset}
            """)
    Flux<LoanPageableDTO> findPageablePendingLoans(Pageable pageable);

}

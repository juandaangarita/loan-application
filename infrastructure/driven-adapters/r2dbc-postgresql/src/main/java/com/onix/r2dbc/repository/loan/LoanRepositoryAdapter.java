package com.onix.r2dbc.repository.loan;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.r2dbc.entity.LoanEntity;
import com.onix.r2dbc.helper.ReactiveAdapterOperations;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LoanRepositoryAdapter extends ReactiveAdapterOperations<
        Loan,
        LoanEntity,
        String,
        LoanReactiveRepository
> implements LoanRepository {
    public LoanRepositoryAdapter(LoanReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Loan.class));
    }

    @Override
    public Mono<Loan> saveLoanApplication(Loan loan) {
        return super.save(loan);
    }

    @Override
    public Flux<LoanPageableDTO> findPendingLoans(int page, int size, String sortBy, String filter) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findPageablePendingLoans(filter, pageable)
                .map(entity -> mapper.map(entity, LoanPageableDTO.class));
    }

    @Override
    public Mono<Loan> findById(UUID loanId) {
        return super.findById(loanId.toString());
    }
}

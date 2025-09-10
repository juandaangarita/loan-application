package com.onix.r2dbc.repository.loanstatus;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loanstatus.LoanStatus;
import com.onix.model.loanstatus.gateways.LoanStatusRepository;
import com.onix.r2dbc.entity.LoanEntity;
import com.onix.r2dbc.entity.LoanStatusEntity;
import com.onix.r2dbc.helper.ReactiveAdapterOperations;
import java.util.UUID;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class LoanStatusRepositoryAdapter extends ReactiveAdapterOperations<
        LoanStatus,
        LoanStatusEntity,
        Integer,
        LoanStatusReactiveRepository
> implements LoanStatusRepository {

    public LoanStatusRepositoryAdapter(LoanStatusReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanStatus.class));
    }


    @Override
    public Mono<LoanStatus> getStatusByName(String name) {
        return repository.findByName(name);
    }
}

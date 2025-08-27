package com.onix.r2dbc.repository.loantype;

import com.onix.model.loantype.LoanType;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.r2dbc.entity.LoanTypeEntity;
import com.onix.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LoanTypeRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        Integer,
        LoanTypeReactiveRepository
> implements LoanTypeRepository {
    public LoanTypeRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanType.class));
    }

    @Override
    public Mono<LoanType> getLoanTypeById(Integer id) {
        return super.findById(id);
    }
}

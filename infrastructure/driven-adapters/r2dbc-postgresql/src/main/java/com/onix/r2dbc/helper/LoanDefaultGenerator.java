package com.onix.r2dbc.helper;

import com.onix.r2dbc.entity.LoanEntity;
import java.util.UUID;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoanDefaultGenerator implements BeforeConvertCallback<LoanEntity> {
    @Override
    public Publisher<LoanEntity> onBeforeConvert(LoanEntity entity, SqlIdentifier table) {
        if (entity.getLoanId() == null) {
            entity.setLoanId(UUID.randomUUID());
        }
        if (entity.getStatusId() == null) {
            entity.setStatusId(1);
        }
        if (entity.getLoanTypeId() == null) {
            entity.setLoanTypeId(1);
        }
        return Mono.just(entity);
    }
}

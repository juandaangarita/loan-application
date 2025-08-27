package com.onix.r2dbc.helper;

import com.onix.r2dbc.dto.LoanEntity;
import java.util.UUID;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoanIdGenerator implements BeforeConvertCallback<LoanEntity> {
    @Override
    public Publisher<LoanEntity> onBeforeConvert(LoanEntity entity, SqlIdentifier table) {
        if (entity.getLoanApplicationId() == null) {
            entity.setLoanApplicationId(UUID.randomUUID());
        }
        return Mono.just(entity);
    }
}

package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.usecase.loanapplication.LoanUseCase;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanHandler {

    private final LoanUseCase loanUseCase;
    private final LoanConfig loanConfig;
    private final LoanMapper loanMapper;
    private final LoggingLoanValidator loggingLoanValidator;
    private final TransactionalOperator transactionalOperator;

//    public Mono<ServerResponse> listenSaveLoan(ServerRequest request) {
//        log.trace("Submitted new loan application request");
//        return request.bodyToMono(CreateLoanDTO.class)
//                .doOnNext(dto -> log.trace("Request body: {}", dto))
//                .map(loanMapper::toModel)
//                .flatMap(loan -> loggingLoanValidator.validate(loan).thenReturn(loan))
//                .flatMap(loanUseCase::createLoanApplication)
//                .as(transactionalOperator::transactional)
//                .map(loanMapper::toDto)
//                .doOnNext(loanDTO -> log.debug("Loan submitted successfully with ID: {}", loanDTO.loanId()))
//                .flatMap(loanDTO -> ServerResponse
//                        .created(URI.create(loanConfig.getLoan() + loanDTO.loanId()))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(ApiResponse.success(
//                                HttpStatus.CREATED.value(),
//                                "Loan submitted successfully",
//                                loanDTO))
//                );
//    }

    public Mono<ServerResponse> listenSaveLoan(ServerRequest request) {
        log.trace("Submitted new loan application request");
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);

        return Mono.zip(
                        request.bodyToMono(CreateLoanDTO.class)
                                .doOnNext(dto -> log.trace("Request body: {}", dto))
                                .map(loanMapper::toModel)
                                .flatMap(loan -> loggingLoanValidator.validate(loan).thenReturn(loan)),
                        request.principal()
                )
                .flatMap(tuple -> {
                    var loan = tuple.getT1();
                    var principal = tuple.getT2();

                    String emailFromToken = principal.getName();

                    if (!loan.getEmail().equals(emailFromToken)) {
                        log.warn("Unauthorized loan request. Token email: {}, Loan email: {}", emailFromToken, loan.getEmail());
                        return Mono.error(new UnauthorizedClientException(loan.getEmail()));
                    }

                    return loanUseCase.createLoanApplication(loan, token)
                            .as(transactionalOperator::transactional)
                            .map(loanMapper::toDto)
                            .doOnNext(loanDTO -> log.debug("Loan submitted successfully with ID: {}", loanDTO.loanId()))
                            .flatMap(loanDTO -> ServerResponse
                                    .created(URI.create(loanConfig.getLoan() + loanDTO.loanId()))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.success(
                                            HttpStatus.CREATED.value(),
                                            "Loan submitted successfully",
                                            loanDTO))
                            );
                });
    }
}

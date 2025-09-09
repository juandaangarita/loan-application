package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.shared.dto.ApiResponse;
import com.onix.usecase.loanapplication.LoanUseCase;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ServerResponse> listenFilterLoans(ServerRequest request) {
        log.trace("Received request to filter loans");
        String token = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(2);
        String sortBy = request.queryParam("sortBy").orElse("email");
        String filter = request.queryParam("filter").orElse("Review Pending");
        return loanUseCase.getPendingLoans(page, size, sortBy, filter, token)
                .as(transactionalOperator::transactional)
                .doOnNext(loanDTO -> log.debug("Loan retrieved successfully with ID: {}", loanDTO.loanId()))
                .collectList()
                .flatMap(loanDTOs -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.OK.value(),
                                "Loans retrieved successfully",
                                loanDTOs))
                );
    }


}

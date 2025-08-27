package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.usecase.loanapplication.LoanUseCase;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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

    public Mono<ServerResponse> listenSaveUser(ServerRequest request) {
        log.trace("Received request to create a new user");
        return request.bodyToMono(CreateLoanDTO.class)
                .doOnNext(dto -> log.trace("Request body: {}", dto))
                .map(loanMapper::toModel)
                .flatMap(loan -> loggingLoanValidator.validate(loan).thenReturn(loan))
                .flatMap(loanUseCase::createLoanApplication)
                .map(loanMapper::toDto)
                .doOnNext(loanDTO -> log.debug("Loan submitted successfully with ID: {}", loanDTO.loanId()))
                .flatMap(loanDTO -> ServerResponse
                        .created(URI.create(loanConfig.getUsers() + loanDTO.loanId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(
                                HttpStatus.CREATED.value(),
                                "Loan submitted successfully",
                                loanDTO))
                );
    }
}

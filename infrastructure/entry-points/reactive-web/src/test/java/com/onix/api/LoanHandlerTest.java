package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.PageDTO;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.usecase.loanapplication.LoanUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LoanHandlerTest {

    @Mock
    private LoanUseCase loanUseCase;
    @Mock
    private LoanConfig loanConfig;
    @Mock
    private LoanMapper loanMapper;
    @Mock
    private LoggingLoanValidator loggingLoanValidator;
    @Mock
    private TransactionalOperator transactionalOperator;
    @Mock
    private ServerRequest serverRequest;
    @Mock
    private ServerRequest.Headers headers;
    @InjectMocks
    private LoanHandler loanHandler;

    private static final String VALID_TOKEN = "Bearer valid-token";
    private static final String USER_EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        lenient().when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldSuccessfullySaveLoanApplication() {
        // Arrange
        CreateLoanDTO createLoanDTO = new CreateLoanDTO(
                BigDecimal.valueOf(1000L),
                12,
                USER_EMAIL,
                "1234",
                1);
        Loan loanModel = new Loan();
        loanModel.setEmail(USER_EMAIL);
        LoanDTO loanDTO = new LoanDTO(UUID.randomUUID(), BigDecimal.valueOf(1000L),
                12,
                USER_EMAIL,
                "1234",
                1,
                1);

        Principal principal = new UsernamePasswordAuthenticationToken(USER_EMAIL, null);
        when(serverRequest.headers()).thenReturn(headers);
        when(headers.firstHeader(HttpHeaders.AUTHORIZATION)).thenReturn(VALID_TOKEN);

        when(serverRequest.bodyToMono(CreateLoanDTO.class)).thenReturn(Mono.just(createLoanDTO));
        doReturn(Mono.just(principal)).when(serverRequest).principal();

        when(loanMapper.toModel(createLoanDTO)).thenReturn(loanModel);
        when(loggingLoanValidator.validate(any(Loan.class))).thenReturn(Mono.empty());
        when(loanUseCase.createLoanApplication(any(Loan.class), anyString())).thenReturn(Mono.just(loanModel));
        when(loanMapper.toDto(loanModel)).thenReturn(loanDTO);
        when(loanConfig.getLoan()).thenReturn("https://api.test/loans/");

        // Act
        Mono<ServerResponse> responseMono = loanHandler.listenSaveLoan(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.CREATED, serverResponse.statusCode());
                    assertEquals(URI.create("https://api.test/loans/" + loanDTO.loanId()), serverResponse.headers().getLocation());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnUnauthorizedWhenEmailDoesNotMatch() {
        // Arrange
        String otherEmail = "other@email.com";
        CreateLoanDTO createLoanDTO = new CreateLoanDTO(BigDecimal.valueOf(1000L),
                12,
                otherEmail,
                "1234",
                1);
        Loan loanModel = new Loan();
        loanModel.setEmail(otherEmail);

        Principal principal = new UsernamePasswordAuthenticationToken(USER_EMAIL, null);
        when(serverRequest.headers()).thenReturn(headers);
        when(headers.firstHeader(HttpHeaders.AUTHORIZATION)).thenReturn(VALID_TOKEN);

        when(serverRequest.bodyToMono(CreateLoanDTO.class)).thenReturn(Mono.just(createLoanDTO));
        doReturn(Mono.just(principal)).when(serverRequest).principal();

        when(loanMapper.toModel(createLoanDTO)).thenReturn(loanModel);
        when(loggingLoanValidator.validate(any(Loan.class))).thenReturn(Mono.empty());

        // Act
        Mono<ServerResponse> responseMono = loanHandler.listenSaveLoan(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UnauthorizedClientException &&
                                throwable.getMessage().contains(otherEmail)
                )
                .verify();
    }

    @Test
    void shouldSuccessfullyFilterLoansWithAllParams() {
        // Arrange
        PageDTO pageDTO = new PageDTO(
                List.of(new LoanDTO(UUID.randomUUID(), BigDecimal.valueOf(1000L),
                        12,
                        USER_EMAIL,
                        "1234",
                        1,
                        1)),
                0, 1, 1L, 1, false, false
        );

        when(serverRequest.headers()).thenReturn(headers);
        when(headers.firstHeader(HttpHeaders.AUTHORIZATION)).thenReturn(VALID_TOKEN);

        when(serverRequest.queryParam("page")).thenReturn(Optional.of("0"));
        when(serverRequest.queryParam("size")).thenReturn(Optional.of("1"));
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.of("email"));
        when(serverRequest.queryParam("status")).thenReturn(Optional.of("Pending Review"));
        when(loanUseCase.getPendingLoans(0, 1, "email", "Pending Review", VALID_TOKEN)).thenReturn(Mono.just(pageDTO));

        // Act
        Mono<ServerResponse> responseMono = loanHandler.listenFilterLoans(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldSuccessfullyFilterLoansWithDefaultParams() {
        // Arrange
        PageDTO pageDTO = new PageDTO(
                List.of(new LoanDTO(UUID.randomUUID(), BigDecimal.valueOf(1000L),
                        12,
                        USER_EMAIL,
                        "1234",
                        1,
                        1)),
                0, 1, 1L, 1, false, false
        );
        when(serverRequest.headers()).thenReturn(headers);
        when(headers.firstHeader(HttpHeaders.AUTHORIZATION)).thenReturn(VALID_TOKEN);

        when(serverRequest.queryParam("page")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("size")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("sortBy")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("status")).thenReturn(Optional.empty());
        when(loanUseCase.getPendingLoans(0, 2, "email", "Pending Review", VALID_TOKEN)).thenReturn(Mono.just(pageDTO));

        // Act
        Mono<ServerResponse> responseMono = loanHandler.listenFilterLoans(serverRequest);

        // Assert
        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.OK, serverResponse.statusCode());
                })
                .verifyComplete();
    }
}
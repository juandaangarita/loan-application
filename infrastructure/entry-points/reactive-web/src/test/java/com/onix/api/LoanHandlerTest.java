package com.onix.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static reactor.core.publisher.Mono.when;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.model.loanapplication.Loan;
import com.onix.usecase.exception.ValidationException;
import com.onix.usecase.loanapplication.LoanUseCase;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanHandlerTest {

    @InjectMocks
    private LoanHandler loanHandler;

    @Mock
    private LoanUseCase loanUseCase;

    @Mock
    private LoanConfig loanConfig;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoggingLoanValidator loggingLoanValidator;

    private Loan loan;
    private LoanDTO loanDTO;
    private CreateLoanDTO createLoanDTO;

    @BeforeEach
    void setup() {
        createLoanDTO = new CreateLoanDTO(
                BigDecimal.valueOf(5000), 1, "email@email.com", "123", 1);

        loan = Loan.builder()
                .loanTypeId(1)
                .amount(new BigDecimal("5000"))
                .documentNumber("123456789")
                .email("email@email.com")
                .termMonths(12)
                .build();

        loanDTO = new LoanDTO(
                UUID.randomUUID(), new BigDecimal("5000"), 1,"email@email.com", "123456789", 1, 1
        );

        lenient().when(loanMapper.toModel(any())).thenReturn(loan);
        lenient().when(loanMapper.toDto(any())).thenReturn(loanDTO);
        lenient().when(loggingLoanValidator.validate(any())).thenReturn(Mono.empty());
        lenient().when(loanUseCase.createLoanApplication(any())).thenReturn(Mono.just(loan));
        lenient().when(loanConfig.getLoan()).thenReturn("/api/v1/loans/");
    }

    @Test
    void shouldCreateLoanSuccessfully() {
        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/api/v1/loans"))
                .body(Mono.just(createLoanDTO));

        Mono<ServerResponse> responseMono = loanHandler.listenSaveLoan(request);

        StepVerifier.create(responseMono)
                .assertNext(serverResponse -> {
                    assertEquals(HttpStatus.CREATED, serverResponse.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldNotCreateLoanWhenValidationFails() {
        when(loggingLoanValidator.validate(any()))
                .thenReturn(Mono.error(new ValidationException(List.of("Invalid loan"))));

        CreateLoanDTO invalidLoanDTO = new CreateLoanDTO(
                BigDecimal.valueOf(5000), 1, "email@email.com", "", 1);


        ServerRequest request = MockServerRequest.builder()
                .method(HttpMethod.POST)
                .uri(URI.create("/api/v1/loans"))
                .body(Mono.just(invalidLoanDTO));

        Mono<ServerResponse> responseMono = loanHandler.listenSaveLoan(request);

        StepVerifier.create(responseMono)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ValidationException.class);
                    assertThat(error.getMessage()).contains("Invalid loan");
                })
                .verify();
    }
}

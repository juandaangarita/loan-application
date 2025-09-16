package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import com.onix.api.mapper.LoanMapper;
import com.onix.api.validator.LoggingLoanValidator;
import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.model.loanapplication.dto.PageDTO;
import com.onix.security.config.SecurityConfig;
import com.onix.security.jwt.JwtFilter;
import com.onix.security.jwt.JwtProvider;
import com.onix.security.jwt.JwtAuthenticationManager;
import com.onix.security.repository.SecurityContextRepository;
import com.onix.usecase.loanapplication.LoanUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, LoanHandler.class})
@EnableConfigurationProperties(LoanConfig.class)
@WebFluxTest
@Import(SecurityConfig.class)
class RouterRestTest {

    private static final String LOANS_PATH = "/api/v1/loan";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private LoanConfig loanConfig;

    @MockitoBean
    private LoanUseCase loanUseCase;

    @MockitoBean
    private LoanMapper loanMapper;

    @MockitoBean
    private LoggingLoanValidator loggingLoanValidator;

    @MockitoBean
    private TransactionalOperator transactionalOperator;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private JwtAuthenticationManager jwtAuthenticationManager;

    @MockitoBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        // Mock the JWT filter to pass through without authentication errors
        when(jwtFilter.filter(any(), any()))
                .thenAnswer(invocation -> {
                    ServerWebExchange exchange = invocation.getArgument(0);
                    WebFilterChain chain = invocation.getArgument(1);
                    return chain.filter(exchange);
                });
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
        when(securityContextRepository.load(any())).thenReturn(Mono.empty());
    }

    @Test
    @WithMockUser(username = "email@email.com", authorities = "ADMIN")
    void shouldSaveLoanApplication() {
        // Arrange
        CreateLoanDTO createLoanDTO = new CreateLoanDTO(
                BigDecimal.valueOf(1000L),
                12,
                "email@email.com",
                "1234",
                1);

        Loan loan = new Loan().toBuilder()
                .loanId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(5000))
                .termMonths(12).email("email@email.com")
                .documentNumber("1234").loanTypeId(1).statusId(1).build();

        LoanDTO loanDTO = new LoanDTO(
                UUID.randomUUID(), BigDecimal.valueOf(5000), 12, "email@email.com", "1234", 1, 1);

        // Mock the behavior of your dependencies
        when(loanMapper.toModel(any())).thenReturn(loan);
        when(loggingLoanValidator.validate(any())).thenReturn(Mono.empty());
        when(loanUseCase.createLoanApplication(any(), any())).thenReturn(Mono.just(loan));
        when(loanMapper.toDto(any())).thenReturn(loanDTO);

        // Act & Assert
        webTestClient.post()
                .uri(LOANS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createLoanDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(201)
                .jsonPath("$.data.email").isEqualTo("email@email.com");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldFilterLoans() {
        // Arrange
        LoanPageableDTO dto1 = new LoanPageableDTO(
                UUID.randomUUID(), BigDecimal.valueOf(5000), 12, "test1@example.com",
                "John Doe", "Personal", BigDecimal.TEN, "Pending Review", null, null);
        PageDTO response = new PageDTO(
                List.of(dto1),
                0,
                10,
                1L,
                1,
                true,
                true);

        when(loanUseCase.getPendingLoans(any(Integer.class), any(Integer.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(response));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(LOANS_PATH)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .queryParam("sortBy", "email")
                        .queryParam("filter", "Pending")
                        .build())
                .header("Authorization", "Bearer mock-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.httpCode").isEqualTo(200)
                .jsonPath("$.data.content[0].email").isEqualTo("test1@example.com");
    }
}


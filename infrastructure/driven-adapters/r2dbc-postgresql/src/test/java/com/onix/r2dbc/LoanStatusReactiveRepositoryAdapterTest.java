package com.onix.r2dbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.r2dbc.entity.LoanEntity;
import com.onix.r2dbc.repository.loan.LoanReactiveRepository;
import com.onix.r2dbc.repository.loan.LoanRepositoryAdapter;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanRepositoryAdapterTest {

    @Mock
    private LoanReactiveRepository repository;
    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private LoanRepositoryAdapter loanRepositoryAdapter;

    private Loan validLoan;
    private LoanEntity loanEntity;
    private LoanPageableDTO loanPageableDTO;

    @BeforeEach
    void setUp() {
        validLoan = new Loan();
        validLoan.setLoanId(UUID.randomUUID());
        validLoan.setAmount(BigDecimal.valueOf(1000));
        validLoan.setEmail("test@example.com");

        loanEntity = new LoanEntity();
        loanEntity.setLoanId(validLoan.getLoanId());
        loanEntity.setAmount(validLoan.getAmount());
        loanEntity.setEmail(validLoan.getEmail());

        loanPageableDTO = new LoanPageableDTO(UUID.randomUUID(), BigDecimal.valueOf(5000), 12, "test1@test.com", "John Doe", "Personal", BigDecimal.TEN, "Pending Review", null, null);

        lenient().when(mapper.map(any(Loan.class), eq(LoanEntity.class))).thenReturn(loanEntity);
        lenient().when(mapper.map(any(LoanEntity.class), eq(Loan.class))).thenReturn(validLoan);
    }

    // --- Tests para saveLoanApplication ---

    @Test
    void shouldSaveLoanApplicationSuccessfully() {
        // Arrange
        when(repository.save(any(LoanEntity.class))).thenReturn(Mono.just(loanEntity));

        // Act
        Mono<Loan> result = loanRepositoryAdapter.saveLoanApplication(validLoan);

        // Assert
        StepVerifier.create(result)
                .expectNext(validLoan)
                .verifyComplete();
        verify(repository, times(1)).save(any(LoanEntity.class));
    }

    @Test
    void shouldReturnEmptyFluxWhenNoPendingLoansFound() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "email";
        String filter = "PENDING";

        when(repository.findPageablePendingLoans(anyString(), any(Pageable.class)))
                .thenReturn(Flux.empty());

        // Act
        Flux<LoanPageableDTO> result = loanRepositoryAdapter.findPendingLoans(page, size, sortBy, filter);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(repository, times(1)).findPageablePendingLoans(filter, PageRequest.of(page, size));
        verify(mapper, never()).map(any(), any());
    }

    // --- Tests para countPendingLoans ---

    @Test
    void shouldCountPendingLoansSuccessfully() {
        // Arrange
        long expectedCount = 5L;
        String filter = "PENDING";
        when(repository.countPendingLoans(filter)).thenReturn(Mono.just(expectedCount));

        // Act
        Mono<Long> result = loanRepositoryAdapter.countPendingLoans(filter);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();
        verify(repository, times(1)).countPendingLoans(filter);
    }
}

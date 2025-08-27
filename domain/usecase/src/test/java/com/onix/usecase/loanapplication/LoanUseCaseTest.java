package com.onix.usecase.loanapplication;


import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loantype.LoanType;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.usecase.exception.InvalidAmountLoanException;
import com.onix.usecase.exception.InvalidLoanTypeException;
import com.onix.usecase.exception.ValidationException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LoanUseCaseTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanValidator loanValidator;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @InjectMocks
    private LoanUseCase loanUseCase;

    private Loan loan;
    private LoanType loanType;

    @BeforeEach
    void setUp() {
        loan = Loan.builder()
                .loanTypeId(1)
                .amount(BigDecimal.valueOf(5000))
                .build();

        loanType = LoanType.builder()
                .loanTypeId(1)
                .minAmount(BigDecimal.valueOf(1000))
                .maxAmount(BigDecimal.valueOf(10000))
                .build();
        lenient().doReturn(Mono.just(loan)).when(loanRepository).saveLoanApplication(loan);
    }

    @Test
    void shouldCreateLoanApplicationWhenValid() {
        doReturn(Mono.empty()).when(loanValidator).validate(loan);
        doReturn(Mono.just(loanType)).when(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());
        doReturn(Mono.just(loan)).when(loanRepository).saveLoanApplication(loan);

        StepVerifier.create(loanUseCase.createLoanApplication(loan))
                .expectNext(loan)
                .verifyComplete();

        verify(loanValidator).validate(loan);
        verify(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());
        verify(loanRepository).saveLoanApplication(loan);
    }

    @Test
    void shouldFailWhenLoanInvalid() {
        doReturn(Mono.error(new ValidationException(List.of("Invalid loan"))))
                .when(loanValidator).validate(loan);
        doReturn(Mono.just(loanType)).when(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());
        doReturn(Mono.just(loan)).when(loanRepository).saveLoanApplication(loan);

        StepVerifier.create(loanUseCase.createLoanApplication(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Invalid loan"))
                .verify();

        verify(loanValidator).validate(loan);
    }

    @Test
    void shouldFailWhenLoanTypeNotFound() {
        doReturn(Mono.empty()).when(loanValidator).validate(loan);
        doReturn(Mono.empty()).when(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());

        StepVerifier.create(loanUseCase.createLoanApplication(loan))
                .expectErrorMatches(ex -> ex instanceof InvalidLoanTypeException &&
                        ex.getMessage().contains(loan.getLoanTypeId().toString()))
                .verify();
    }

    @Test
    void shouldFailWhenAmountBelowMinimum() {
        loan.setAmount(BigDecimal.valueOf(500));

        doReturn(Mono.empty()).when(loanValidator).validate(loan);
        doReturn(Mono.just(loanType)).when(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());

        StepVerifier.create(loanUseCase.createLoanApplication(loan))
                .expectErrorMatches(ex -> ex instanceof InvalidAmountLoanException &&
                        ex.getMessage().contains("500"))
                .verify();
    }

    @Test
    void shouldFailWhenAmountAboveMaximum() {
        loan.setAmount(BigDecimal.valueOf(20000));

        doReturn(Mono.empty()).when(loanValidator).validate(loan);
        doReturn(Mono.just(loanType)).when(loanTypeRepository).getLoanTypeById(loan.getLoanTypeId());

        StepVerifier.create(loanUseCase.createLoanApplication(loan))
                .expectErrorMatches(ex -> ex instanceof InvalidAmountLoanException &&
                        ex.getMessage().contains("20000"))
                .verify();
    }
}
package com.onix.api.validator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.loanapplication.Loan;
import com.onix.model.exception.ValidationException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoggingLoanValidatorTest {

    @Mock
    private LoanValidator loanValidator;

    @InjectMocks
    private LoggingLoanValidator loggingLoanValidator;

    @Test
    void shouldPassWhenValidatorSucceeds() {
        Loan loan = new Loan().builder()
                .loanTypeId(1)
                .amount(BigDecimal.valueOf(5000))
                .email("correo@email.com")
                .documentNumber("1234567890")
                .build();

        when(loanValidator.validate(loan)).thenReturn(Mono.empty());

        StepVerifier.create(loggingLoanValidator.validate(loan))
                .verifyComplete();

        verify(loanValidator).validate(loan);
    }

    @Test
    void shouldFailWhenValidatorFails() {
        Loan loan = new Loan().builder()
                .loanTypeId(1)
                .amount(BigDecimal.valueOf(5000))
                .email("")
                .documentNumber("1234567890")
                .build();

        when(loanValidator.validate(loan))
                .thenReturn(Mono.error(new ValidationException(List.of("Email cannot be null or empty"))));

        StepVerifier.create(loggingLoanValidator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Email cannot be null or empty"))
                .verify();

        verify(loanValidator).validate(loan);
    }
}
package com.onix.usecase.loanapplication.validator;

import com.onix.model.loanapplication.Loan;
import com.onix.model.exception.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LoanValidatorTest {

    private LoanValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LoanValidator();
    }

    @Test
    void shouldPassValidationForValidLoan() {
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(5000))
                .termMonths(12)
                .email("valid@email.com")
                .documentNumber("123456789")
                .build();

        StepVerifier.create(validator.validate(loan))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenAmountIsNull() {
        Loan loan = Loan.builder()
                .termMonths(12)
                .email("valid@email.com")
                .documentNumber("123456789")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Amount cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenTermIsNull() {
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(5000))
                .email("valid@email.com")
                .documentNumber("123456789")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Terms in months cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenEmailIsNull() {
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(5000))
                .termMonths(12)
                .documentNumber("123456789")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Email cannot be null"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenEmailIsInvalid() {
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(5000))
                .termMonths(12)
                .email("invalid-email")
                .documentNumber("123456789")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Email format is invalid"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenDocumentNumberIsNullOrEmpty() {
        Loan loan = Loan.builder()
                .amount(BigDecimal.valueOf(5000))
                .termMonths(12)
                .email("valid@email.com")
                .documentNumber("")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().contains("Document number cannot be null or empty"))
                .verify();
    }

    @Test
    void shouldReturnMultipleErrors() {
        Loan loan = Loan.builder()
                .email("invalid-email")
                .build();

        StepVerifier.create(validator.validate(loan))
                .expectErrorMatches(ex -> ex instanceof ValidationException &&
                        ((ValidationException) ex).getErrors().size() == 4 &&
                        ((ValidationException) ex).getErrors().containsAll(List.of(
                                "Amount cannot be null or empty",
                                "Terms in months cannot be null or empty",
                                "Email format is invalid",
                                "Document number cannot be null or empty"
                        )))
                .verify();
    }
}

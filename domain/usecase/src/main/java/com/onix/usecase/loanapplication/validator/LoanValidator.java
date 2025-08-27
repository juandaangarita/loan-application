package com.onix.usecase.loanapplication.validator;

import com.onix.model.loanapplication.Loan;
import com.onix.usecase.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanValidator {

    public Mono<Void> validate(Loan loan) {
        List<String> errors = new ArrayList<>();
        validateAmount(loan, errors);
        validateTerm(loan, errors);
        validateEmail(loan, errors);
        validateDocumentNumber(loan, errors);

        if (!errors.isEmpty()) {
            return Mono.error(new ValidationException(errors));
        }
        return Mono.empty();
    }

    private void validateAmount(Loan loan, List<String> errors) {
        if (loan.getAmount() == null || isNullOrEmpty(loan.getAmount().toString())) {
            errors.add("Amount cannot be null or empty");
        }
    }

    private void validateTerm(Loan loan, List<String> errors) {
        if (loan.getTermMonths() == null || isNullOrEmpty(loan.getTermMonths().toString())) {
            errors.add("Terms in months cannot be null or empty");
        }
    }

    private void validateEmail(Loan loan, List<String> errors) {
        if (loan.getEmail() == null) {
            errors.add("Email cannot be null");
        } else if (!isValidEmail(loan.getEmail())) {
            errors.add("Email format is invalid");
        }
    }

    private void validateDocumentNumber(Loan loan, List<String> errors) {
        if (isNullOrEmpty(loan.getDocumentNumber())) {
            errors.add("Document number cannot be null or empty");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isBlank();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,}$");
    }
}

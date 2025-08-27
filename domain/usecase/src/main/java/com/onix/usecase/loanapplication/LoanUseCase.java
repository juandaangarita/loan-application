package com.onix.usecase.loanapplication;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loantype.LoanType;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.usecase.exception.InvalidAmountLoanException;
import com.onix.usecase.exception.InvalidLoanTypeException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanValidator loanValidator;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<Loan> createLoanApplication(Loan loan) {
        return loanValidator.validate(loan)
                .then(validateLoan(loan))
                .then(loanRepository.saveLoanApplication(loan));
    }

    public Mono<Void> validateLoan(Loan loan) {
        return loanTypeRepository.getLoanTypeById(loan.getLoanTypeId())
                .switchIfEmpty(Mono.error(new InvalidLoanTypeException(loan.getLoanTypeId())))
                .flatMap(loanType -> {
                    if (loan.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                            loan.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
                        return Mono.<Void>error(new InvalidAmountLoanException(loan.getAmount(), loanType.getMinAmount(), loanType.getMaxAmount()));
                    }
                    return Mono.empty();
                })
                .then();
    }
}

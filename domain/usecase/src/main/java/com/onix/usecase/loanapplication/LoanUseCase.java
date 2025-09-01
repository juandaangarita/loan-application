package com.onix.usecase.loanapplication;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loanapplication.gateways.UserClient;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.model.exception.InvalidAmountLoanException;
import com.onix.model.exception.InvalidLoanTypeException;
import com.onix.model.exception.UnregisteredUserException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanValidator loanValidator;
    private final LoanTypeRepository loanTypeRepository;
    private final UserClient userClient;

    public Mono<Loan> createLoanApplication(Loan loan) {
        return loanValidator.validate(loan)
                .then(validateUser(loan))
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

    public Mono<Void> validateUser(Loan loan) {
        return userClient.validateUserRegistered(loan.getEmail(), loan.getDocumentNumber())
                .flatMap(isRegistered -> {
                    if (isRegistered == null) {
                        return Mono.error(new UnregisteredUserException(loan.getEmail(), loan.getDocumentNumber()));
                    }
                    return Mono.empty();
                });
    }
}

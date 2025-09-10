package com.onix.usecase.loanapplication;

import com.onix.model.exception.LoanNotFoundException;
import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.model.loanapplication.dto.UserDTO;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loanapplication.gateways.LoanStatusPublisher;
import com.onix.model.loanapplication.gateways.UserClient;
import com.onix.model.loanstatus.LoanStatus;
import com.onix.model.loanstatus.gateways.LoanStatusRepository;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.model.exception.InvalidAmountLoanException;
import com.onix.model.exception.InvalidLoanTypeException;
import com.onix.model.exception.UnregisteredUserException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanValidator loanValidator;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanStatusRepository loanStatusRepository;
    private final UserClient userClient;
    private final LoanStatusPublisher sqsPublisher;

    public Mono<Loan> createLoanApplication(Loan loan, String token) {
        return loanValidator.validate(loan)
                .then(validateUser(loan, token))
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

    public Mono<Void> validateUser(Loan loan, String token) {
        return userClient.validateUserRegistered(loan.getEmail(), loan.getDocumentNumber(), token)
                .flatMap(isRegistered -> {
                    if (isRegistered == null) {
                        return Mono.error(new UnregisteredUserException(loan.getEmail(), loan.getDocumentNumber()));
                    }
                    return Mono.empty();
                });
    }

    public Flux<LoanPageableDTO> getPendingLoans(int page, int size, String sortBy, String filter, String token) {
        return loanRepository.findPendingLoans(page, size, sortBy, filter)
                .collectList()
                .flatMapMany(loans -> {
                    Set<String> emails = loans.stream()
                            .map(LoanPageableDTO::email)
                            .collect(Collectors.toSet());

                    return userClient.getUsersByEmails(emails, token)
                            .flatMapMany(userMap -> Flux.fromIterable(loans)
                                    .map(loan -> {
                                        UserDTO user = userMap.get(loan.email());
                                        return new LoanPageableDTO(
                                                loan.loanId(),
                                                loan.amount(),
                                                loan.termMonths(),
                                                loan.email(),
                                                user != null ? user.name() + " " + user.lastname() : null,
                                                loan.loanType(),
                                                loan.interestRate(),
                                                loan.status(),
                                                user != null ? user.baseSalary() : null,
                                                loan.amount().divide(
                                                        BigDecimal.valueOf(loan.termMonths()),
                                                        2,
                                                        RoundingMode.HALF_UP
                                                )
                                        );
                                    })
                            );
                });
    }

    public Mono<Loan> updateLoanStatus(UUID loanId, String status) {
        return loanRepository.findById(loanId)
                .switchIfEmpty(Mono.error(new LoanNotFoundException(loanId)))
                .flatMap(loan ->
                        getStatusIdByName(status)
                                .flatMap(loanStatus -> {
                                    if (loanStatus.getLoanStatusId().intValue() == loan.getLoanStatusId().intValue()) {
                                        return Mono.error(new IllegalArgumentException(
                                                "Loan is already in status: " + status
                                        ));
                                    }
                                    loan.setLoanStatusId(loanStatus.getLoanStatusId());
                                    return loanRepository.saveLoanApplication(loan);
                                })
                )
                .flatMap(updatedLoan ->
                        sqsPublisher.sendStatusUpdate(updatedLoan, status)
                                .thenReturn(updatedLoan)
                );
    }

    private Mono<LoanStatus> getStatusIdByName(String statusName) {
        return loanStatusRepository.getStatusByName(statusName)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid status name: " + statusName)));
    }
}

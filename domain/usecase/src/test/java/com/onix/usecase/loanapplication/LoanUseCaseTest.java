package com.onix.usecase.loanapplication;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onix.model.exception.UnregisteredUserException;
import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.LoanPageableDTO;
import com.onix.model.loanapplication.dto.UserDTO;
import com.onix.model.loanapplication.gateways.LoanRepository;
import com.onix.model.loanapplication.gateways.UserClient;
import com.onix.model.loantype.LoanType;
import com.onix.model.loantype.gateways.LoanTypeRepository;
import com.onix.model.exception.InvalidAmountLoanException;
import com.onix.model.exception.InvalidLoanTypeException;
import com.onix.usecase.loanapplication.validator.LoanValidator;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LoanUseCaseTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanValidator loanValidator;
    @Mock
    private LoanTypeRepository loanTypeRepository;
    @Mock
    private UserClient userClient;
    @InjectMocks
    private LoanUseCase loanUseCase;

    private Loan validLoan;
    private LoanType validLoanType;
    private String token;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        validLoan = new Loan();
        validLoan.setLoanId(UUID.randomUUID());
        validLoan.setEmail("test@example.com");
        validLoan.setDocumentNumber("12345");
        validLoan.setLoanTypeId(1);
        validLoan.setAmount(BigDecimal.valueOf(5000));
        validLoan.setTermMonths(12);

        validLoanType = new LoanType();
        validLoanType.setLoanTypeId(1);
        validLoanType.setMinAmount(BigDecimal.valueOf(1000));
        validLoanType.setMaxAmount(BigDecimal.valueOf(10000));

        token = "test-token";
        userDTO = new UserDTO(UUID.randomUUID(), "John", "Doe", null, null, null, validLoan.getEmail(), 5000L);
        lenient().when(userClient.getUsersByEmails(anySet(), anyString())).thenReturn(Mono.just(Map.of()));
    }

    // --- Tests for createLoanApplication ---

    @Test
    void shouldCreateLoanApplicationSuccessfully() {
        // Arrange
        when(loanValidator.validate(validLoan)).thenReturn(Mono.empty());
        when(userClient.validateUserRegistered(anyString(), anyString(), anyString())).thenReturn(Mono.just(userDTO));
        when(loanTypeRepository.getLoanTypeById(anyInt())).thenReturn(Mono.just(validLoanType));
        when(loanRepository.saveLoanApplication(validLoan)).thenReturn(Mono.just(validLoan));

        // Act & Assert
        StepVerifier.create(loanUseCase.createLoanApplication(validLoan, token))
                .expectNext(validLoan)
                .verifyComplete();
        verify(loanRepository, times(1)).saveLoanApplication(validLoan);
    }

//    @Test
//    void shouldFailCreateLoanWhenUserIsNotRegistered() {
//        // Arrange
//        when(loanValidator.validate(validLoan)).thenReturn(Mono.empty());
//        when(userClient.validateUserRegistered(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
//        when(loanTypeRepository.getLoanTypeById(anyInt())).thenReturn(Mono.just(validLoanType));
//        when(loanRepository.saveLoanApplication(any())).thenReturn(Mono.just(new Loan()));
//
//        // Act & Assert
//        StepVerifier.create(loanUseCase.createLoanApplication(validLoan, token))
//                .expectError(UnregisteredUserException.class)
//                .verify();
//
//        // Assert that the save method was never called because the flow was interrupted by an error.
//        verify(loanRepository, never()).saveLoanApplication(any());
//    }

    // --- Tests for validateLoan ---

    @Test
    void shouldValidateLoanSuccessfullyWhenAmountIsInValidRange() {
        // Arrange
        when(loanTypeRepository.getLoanTypeById(validLoan.getLoanTypeId())).thenReturn(Mono.just(validLoanType));

        // Act & Assert
        StepVerifier.create(loanUseCase.validateLoan(validLoan))
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenLoanTypeNotFound() {
        // Arrange
        when(loanTypeRepository.getLoanTypeById(validLoan.getLoanTypeId())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loanUseCase.validateLoan(validLoan))
                .expectError(InvalidLoanTypeException.class)
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenAmountIsInvalid() {
        // Arrange
        validLoan.setAmount(BigDecimal.valueOf(15000));
        when(loanTypeRepository.getLoanTypeById(validLoan.getLoanTypeId())).thenReturn(Mono.just(validLoanType));

        // Act & Assert
        StepVerifier.create(loanUseCase.validateLoan(validLoan))
                .expectError(InvalidAmountLoanException.class)
                .verify();
    }

    // --- Tests for validateUser ---

    @Test
    void shouldValidateUserSuccessfully() {
        // Arrange
        when(userClient.validateUserRegistered(validLoan.getEmail(), validLoan.getDocumentNumber(), token)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(loanUseCase.validateUser(validLoan, token))
                .verifyComplete();
    }

//    @Test
//    void shouldThrowExceptionWhenUserIsNotRegistered() {
//        // Arrange
//        when(userClient.validateUserRegistered(anyString(), anyString(), anyString())).thenReturn(Mono.justOrEmpty(Optional.empty()));
//
//        // Act & Assert
//        StepVerifier.create(loanUseCase.validateUser(validLoan, token))
//                .expectErrorSatisfies(throwable -> {
//                    assertTrue(throwable.isInstanceOf(UnregisteredUserException.class));
//                    assertTrue(throwable.getMessage().contains(validLoan.getEmail());
//                })
//                .verify();
//    }

    // --- Tests for getPendingLoans ---

    @Test
    void shouldGetPendingLoansAndEnrichWithUserDataSuccessfully() {
        // Arrange
        int page = 0;
        int size = 1;
        String sortBy = "email";
        String filter = "Pending Review";
        String testEmail = "test@example.com";

        LoanPageableDTO loanPageableDTO = new LoanPageableDTO(
                UUID.randomUUID(), BigDecimal.valueOf(5000), 12, testEmail,
                null, "Personal", BigDecimal.valueOf(10), "Pending Review", null, null
        );
        UserDTO userDTO = new UserDTO(UUID.randomUUID(), "John", "Doe", null, null, null, testEmail, 5000L);

        when(loanRepository.findPendingLoans(page, size, sortBy, filter)).thenReturn(Flux.just(loanPageableDTO));
        when(loanRepository.countPendingLoans(filter)).thenReturn(Mono.just(1L));
        when(userClient.getUsersByEmails(anySet(), eq(token))).thenReturn(Mono.just(Map.of(testEmail, userDTO)));

        // Act & Assert
        StepVerifier.create(loanUseCase.getPendingLoans(page, size, sortBy, filter, token))
                .assertNext(pageDTO -> {
                    assertEquals(1, pageDTO.content().size());
                    assertEquals(1L, pageDTO.totalElements());
                    assertEquals(1, pageDTO.totalPages());
                    assertEquals("John Doe", pageDTO.content().get(0).userName());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoLoansFound() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "email";
        String filter = "Pending Review";

        when(loanRepository.findPendingLoans(page, size, sortBy, filter)).thenReturn(Flux.empty());
        when(loanRepository.countPendingLoans(filter)).thenReturn(Mono.just(0L));

        // Act & Assert
        StepVerifier.create(loanUseCase.getPendingLoans(page, size, sortBy, filter, token))
                .assertNext(pageDTO -> {
                    assertTrue(pageDTO.content().isEmpty());
                    assertEquals(0L, pageDTO.totalElements());
                    assertEquals(0, pageDTO.totalPages());
                })
                .verifyComplete();
    }
}
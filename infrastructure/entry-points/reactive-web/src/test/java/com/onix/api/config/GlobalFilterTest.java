package com.onix.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.onix.model.exception.AuthenticationServiceUnavailableException;
import com.onix.model.exception.InvalidAmountLoanException;
import com.onix.model.exception.InvalidLoanTypeException;
import com.onix.model.exception.UnregisteredUserException;
import com.onix.model.exception.ValidationException;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.shared.dto.ApiResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalFilterTest {

    @Mock
    private WebFilterChain filterChain;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GlobalFilter globalFilter;

    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // Initialize the exchange object before each test
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);

        // Stub the objectMapper to avoid real JSON processing,
        // unless we are specifically testing a JsonProcessingException.
        lenient().when(objectMapper.writeValueAsBytes(any(ApiResponse.class)))
                .thenReturn("{}".getBytes());
    }

    @Test
    void shouldPassThroughWhenNoExceptionThrown() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    // --- Tests for specific exceptions handled by the filter ---

    @Test
    void shouldHandleInvalidAmountLoanException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new InvalidAmountLoanException(BigDecimal.valueOf(1000L), BigDecimal.valueOf(5000L), BigDecimal.valueOf(10000L))));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleInvalidLoanTypeException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new InvalidLoanTypeException(1)));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleValidationException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new ValidationException(List.of("Validation failed"))));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleUnregisteredUserException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new UnregisteredUserException("test@test.com", "12345")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleAuthenticationServiceUnavailableException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new AuthenticationServiceUnavailableException("email@email.com", "12345")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new IllegalArgumentException("Invalid argument")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleInvalidCredentialsException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new InvalidCredentialsException("Invalid token")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldHandleUnauthorizedClientException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new UnauthorizedClientException("Client not authorized")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    // --- Tests for ServerWebInputException and default case ---

    @Test
    void shouldHandleServerWebInputException_WithInvalidFormat() {
        // Arrange
        String errorMessage = "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'";
        InvalidFormatException invalidFormatEx = new InvalidFormatException(
                null, errorMessage, "invalidValue", Integer.class);
        ServerWebInputException serverInputEx = new ServerWebInputException(
                errorMessage);

        when(filterChain.filter(exchange)).thenReturn(Mono.error(serverInputEx));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Assert response status and content type
        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());

        // Assert response body content
        // You'll need to read the DataBuffer to verify the message.
        // For simplicity, we can verify the status code and content type.
    }

    @Test
    void shouldHandleGenericUnhandledException() {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new RuntimeException("An unhandled error occurred")));

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }

    @Test
    void shouldThrowErrorOnJsonProcessingException() throws JsonProcessingException {
        // Arrange
        when(filterChain.filter(exchange)).thenReturn(Mono.error(new InvalidAmountLoanException(BigDecimal.valueOf(1000L), BigDecimal.valueOf(5000L), BigDecimal.valueOf(10000L))));
        when(objectMapper.writeValueAsBytes(any(ApiResponse.class))).thenThrow(new JsonProcessingException("Test error") {});

        // Act & Assert
        StepVerifier.create(globalFilter.filter(exchange, filterChain))
                .expectError(JsonProcessingException.class)
                .verify();

        assertEquals(HttpStatus.BAD_REQUEST, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
}
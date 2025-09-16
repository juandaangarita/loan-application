package com.onix.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.onix.model.exception.InvalidAmountLoanException;
import com.onix.model.exception.InvalidLoanTypeException;
import com.onix.model.exception.LoanNotFoundException;
import com.onix.model.exception.UnregisteredUserException;
import com.onix.model.exception.AuthenticationServiceUnavailableException;
import com.onix.model.exception.ValidationException;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalFilter implements WebFilter {

    private static final String VALIDATION_ERROR = "Validation error";

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> handleException(exchange, throwable));
    }

    private Mono<Void> handleException(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        ApiResponse<Object> body;

        switch (ex) {
            case InvalidAmountLoanException invalidAmountLoanException -> {
                log.debug("Invalid amount for type of loan exception: {}", invalidAmountLoanException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), VALIDATION_ERROR, ex.getMessage());
            }
            case InvalidLoanTypeException invalidLoanTypeException -> {
                log.debug("Invalid type of loan exception: {}", invalidLoanTypeException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), VALIDATION_ERROR, ex.getMessage());
            }
            case ValidationException validationException -> {
                log.debug("Validation exception: {}", validationException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), VALIDATION_ERROR, ex.getMessage());
            }
            case UnregisteredUserException unregisteredUserException -> {
                log.debug("Unregistered client exception: {}", unregisteredUserException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), "User Not Found", ex.getMessage());
            }
            case AuthenticationServiceUnavailableException userServiceUnavailableException -> {
                log.debug("Calling authentication service but service is unavailable: {}", userServiceUnavailableException.getMessage());
                status = HttpStatus.SERVICE_UNAVAILABLE;
                body = ApiResponse.error(status.value(), "Authentication Service Unavailable", ex.getMessage());
            }
            case IllegalArgumentException illegalArgumentException -> {
                log.debug("Illegal argument exception: {}", illegalArgumentException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), VALIDATION_ERROR, ex.getMessage());
            }
            case InvalidCredentialsException invalidCredentialsException -> {
                log.debug("Invalid credentials: {}", invalidCredentialsException.getMessage());
                status = HttpStatus.UNAUTHORIZED;
                body = ApiResponse.error(status.value(), "Unauthorized", ex.getMessage());
            }
            case UnauthorizedClientException unauthorizedClientException -> {
                log.debug(unauthorizedClientException.getMessage());
                status = HttpStatus.UNAUTHORIZED;
                body = ApiResponse.error(status.value(), VALIDATION_ERROR, ex.getMessage());
            }
            case LoanNotFoundException loanNotFoundException -> {
                log.debug("Loan Not Found: {}", loanNotFoundException.getMessage());
                status = HttpStatus.BAD_REQUEST;
                body = ApiResponse.error(status.value(), "Unauthorized", ex.getMessage());
            }
            case AuthorizationDeniedException authorizationDeniedException -> {
                log.debug("Authorization Denied: {}", authorizationDeniedException.getMessage());
                status = HttpStatus.FORBIDDEN;
                body = ApiResponse.error(status.value(), "Forbidden", ex.getMessage());
            }
            case ServerWebInputException serverWebInputException -> {
                log.debug("Server web input exception: {}", serverWebInputException.getMessage());
                status = HttpStatus.BAD_REQUEST;

                Throwable cause = findCause(serverWebInputException, InvalidFormatException.class);
                String field = "unknown";

                if (cause instanceof InvalidFormatException invalidFormat && !invalidFormat.getPath().isEmpty()) {
                    field = invalidFormat.getPath().getFirst().getFieldName();
                }

                body = ApiResponse.error(status.value(), "Validation error",
                        "Invalid type for field " + field);
                log.debug("Invalid format exception for field: {}", field);
            }
            default -> {
                log.error("Unhandled exception: {}", ex.getMessage(), ex);
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                body = ApiResponse.error(status.value(), "Internal server error", ex.getMessage());
            }
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer dataBuffer;
        try {
            dataBuffer = exchange.getResponse().bufferFactory()
                    .wrap(objectMapper.writeValueAsBytes(body));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private <T extends Throwable> Throwable findCause(Throwable throwable, Class<T> clazz) {
        Throwable current = throwable;
        while (current != null) {
            if (clazz.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}

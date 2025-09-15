package com.onix.consumer;

import com.onix.consumer.config.UserPropertiesConfig;

import com.onix.model.exception.UnregisteredUserException;
import com.onix.model.exception.AuthenticationServiceUnavailableException;
import com.onix.model.loanapplication.dto.UserBatchRequestDTO;
import com.onix.model.loanapplication.dto.UserBatchResponseDTO;
import com.onix.model.loanapplication.dto.UserDTO;
import com.onix.model.loanapplication.dto.UserResponse;
import com.onix.model.loanapplication.gateways.UserClient;
import com.onix.shared.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestConsumer implements UserClient {

    private final WebClient client;
    private final UserPropertiesConfig userPropertiesConfig;

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackValidateUser")
    public Mono<UserDTO> validateUserRegistered(String email, String documentNumber, String token) {
        String path = userPropertiesConfig.getValidateUserPath()
                + "?email=" + email
                + "&documentNumber=" + documentNumber;
        log.debug("Calling user service to validate user with email: {} and documentNumber: {}", email, documentNumber);
        return client.get()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnNext(response -> log.debug("Received response: {}", response))
                .flatMap(response -> {
                    if (response.getData() == null) {
                        return Mono.error(new UnregisteredUserException(email, documentNumber));
                    }
                    return Mono.just(response.getData());
                })
                .doOnNext(user -> log.info("User found: {}", user))
                .switchIfEmpty(Mono.error(new UnregisteredUserException(email, documentNumber)));
    }

    @Override
    public Mono<Map<String, UserDTO>> getUsersByEmails(Set<String> emails, String token) {
        return client.post()
                .uri(userPropertiesConfig.getBatchUsersPath())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(new UserBatchRequestDTO(emails))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<String, UserDTO>>>() {})
                .map(ApiResponse::data);
    }

    Mono<UserDTO> fallbackValidateUser(String email, String documentNumber, Throwable ex) {
        log.error("User service unavailable. email: {}, documentNumber: {}, cause: {}",
                email, documentNumber, ex.getMessage());

        return Mono.error(new AuthenticationServiceUnavailableException(email, documentNumber));
    }

}

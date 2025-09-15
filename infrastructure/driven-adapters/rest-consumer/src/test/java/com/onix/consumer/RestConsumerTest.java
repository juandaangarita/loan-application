package com.onix.consumer;


import com.onix.consumer.config.UserPropertiesConfig;
import com.onix.model.exception.AuthenticationServiceUnavailableException;
import com.onix.model.exception.UnregisteredUserException;
import com.onix.model.loanapplication.dto.UserDTO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestConsumerTest {

    public static MockWebServer mockBackEnd;
    private WebClient webClient;
    private UserPropertiesConfig userPropertiesConfig;
    private RestConsumer restConsumer;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() throws InterruptedException {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.userPropertiesConfig = new UserPropertiesConfig();
        this.userPropertiesConfig.setValidateUserPath("/api/v1/user/validate");
        this.userPropertiesConfig.setBatchUsersPath("/api/v1/users/batch");
        this.restConsumer = new RestConsumer(this.webClient, this.userPropertiesConfig);
    }

    @Test
    void shouldValidateUserSuccessfully() throws Exception {
        // Arrange
        String email = "test@example.com";
        String documentNumber = "12345";
        String token = "valid_token";
        UserDTO userDTO = new UserDTO(UUID.randomUUID(),
                "User",
                "User",
                LocalDate.of(2000, 1, 1),
                "Address",
                "1234567890",
                email,
                1000L);

        String jsonResponse = """
            {
              "data": {
                "email": "test@example.com"
              }
            }
            """;

        mockBackEnd.enqueue(
                new MockResponse()
                        .setBody(jsonResponse)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        // Act & Assert
        StepVerifier.create(restConsumer.validateUserRegistered(email, documentNumber, token))
                .expectNextMatches(user -> user.email().equals(email))
                .verifyComplete();

        // Verify the request details
        assertEquals("/api/v1/user/validate?email=test@example.com&documentNumber=12345", mockBackEnd.takeRequest().getPath());
    }

    @Test
    void shouldThrowUnregisteredUserExceptionWhenResponseIsEmpty() throws Exception {
        // Arrange
        String email = "test@example.com";
        String documentNumber = "12345";
        String token = "valid_token";

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setBody("{}")
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        // Act & Assert
        StepVerifier.create(restConsumer.validateUserRegistered(email, documentNumber, token))
                .expectError(UnregisteredUserException.class)
                .verify();
        mockBackEnd.takeRequest();
    }

    @Test
    void shouldThrowExceptionWhenUserServiceReturnsError() throws Exception {
        // Arrange
        String email = "test@example.com";
        String documentNumber = "12345";
        String token = "valid_token";

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
        );

        // Act & Assert
        StepVerifier.create(restConsumer.validateUserRegistered(email, documentNumber, token))
                .expectError()
                .verify();
        mockBackEnd.takeRequest();
    }

    @Test
    void shouldGetUsersByEmailsSuccessfully() throws Exception {
        // Arrange
        Set<String> emails = Set.of("test1@example.com", "test2@example.com");
        String token = "valid_token";

        String jsonResponse = """
            {
              "data": {
                "test1@example.com": { "email": "test1@example.com" },
                "test2@example.com": { "email": "test2@example.com" }
              }
            }
            """;

        mockBackEnd.enqueue(
                new MockResponse()
                        .setBody(jsonResponse)
                        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        // Act & Assert
        StepVerifier.create(restConsumer.getUsersByEmails(emails, token))
                .expectNextMatches(users -> users.size() == 2 && users.containsKey("test1@example.com"))
                .verifyComplete();

        // Verify the request details and consume the request to clean the queue.
        assertEquals("/api/v1/users/batch", mockBackEnd.takeRequest().getPath());
    }

    @Test
    void fallbackValidateUserShouldThrowAuthenticationServiceUnavailableException() {
        // Arrange
        String email = "test@example.com";
        String documentNumber = "12345";
        Throwable ex = new RuntimeException("Simulated service failure");

        // Act
        Mono<UserDTO> result = restConsumer.fallbackValidateUser(email, documentNumber, ex);

        // Assert
        StepVerifier.create(result)
                .expectError(AuthenticationServiceUnavailableException.class)
                .verify();
    }
}
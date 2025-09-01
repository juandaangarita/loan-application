package com.onix.consumer;


import com.onix.consumer.config.UserPropertiesConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import java.io.IOException;


class RestConsumerTest {

    private static RestConsumer restConsumer;

    private static MockWebServer mockBackEnd;

    private static UserPropertiesConfig userPropertiesConfig;


    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        userPropertiesConfig = new UserPropertiesConfig();
        userPropertiesConfig.setBaseUrl(mockBackEnd.url("/").toString());
        userPropertiesConfig.setValidateUserPath("/validateUser");
        userPropertiesConfig.setTimeout(1000);
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumer(webClient, userPropertiesConfig);
    }

    @AfterAll
    static void tearDown() throws IOException {

        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Validate the function testGet.")
    void validateTestGet() {

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"email\" : \"email\"}"));
        var response = restConsumer.validateUserRegistered("email@email.com", "1234567890");

        StepVerifier.create(response)
                .expectNextMatches(objectResponse -> objectResponse.email().equals("email"))
                .verifyComplete();
    }

}
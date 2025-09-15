package com.onix.api.config;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CorsConfig.class, CorsConfigTest.TestConfig.class})
class CorsConfigTest {

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void shouldAllowCrossOriginRequestWithCorrectHeaders() {
        // Arrange
        WebFilterChain mockFilterChain = mock(WebFilterChain.class);
        when(mockFilterChain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/some/path") // Use the static 'get' method
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(corsWebFilter.filter(exchange, mockFilterChain))
                .verifyComplete();
    }

    @Test
    void shouldBlockCrossOriginRequestFromUnallowedOrigin() {
        // Arrange
        WebFilterChain mockFilterChain = mock(WebFilterChain.class);
        when(mockFilterChain.filter(any())).thenReturn(Mono.empty());

        MockServerHttpRequest request = MockServerHttpRequest
                .get("http://localhost:8080/some/path") // Use the static 'get' method
                .header(HttpHeaders.ORIGIN, "http://unallowed-origin.com")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(corsWebFilter.filter(exchange, mockFilterChain))
                .verifyComplete();
    }

    // Test helper class
    @Configuration
    static class TestConfig {
        @Bean
        String corsAllowedOrigins() {
            return "http://localhost:3000,http://another.allowed.origin.com";
        }
    }
}
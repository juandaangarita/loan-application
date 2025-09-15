package com.onix.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersConfigTest {

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerHttpResponse response;

    @InjectMocks
    private SecurityHeadersConfig securityHeadersConfig;

    @Test
    void shouldAddAllSecurityHeadersToResponse() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        when(exchange.getResponse()).thenReturn(response);
        when(response.getHeaders()).thenReturn(headers);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = securityHeadersConfig.filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        assertEquals("default-src 'self'; frame-ancestors 'self'; form-action 'self'", headers.get("Content-Security-Policy").get(0));
        assertEquals("max-age=31536000;", headers.get("Strict-Transport-Security").get(0));
        assertEquals("nosniff", headers.get("X-Content-Type-Options").get(0));
        assertEquals("", headers.get("Server").get(0));
        assertEquals("no-store", headers.get("Cache-Control").get(0));
        assertEquals("no-cache", headers.get("Pragma").get(0));
        assertEquals("strict-origin-when-cross-origin", headers.get("Referrer-Policy").get(0));
    }
}
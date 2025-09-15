package com.onix.security.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.onix.security.jwt.JwtAuthenticationManager;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SecurityContextRepositoryTest {

    @Mock
    private JwtAuthenticationManager jwtAuthenticationManager;

    @InjectMocks
    private SecurityContextRepository securityContextRepository;

    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);
    }

    @Test
    void shouldLoadSecurityContextWithValidToken() {
        // Arrange
        String token = "valid_token";
        Authentication authentication = new UsernamePasswordAuthenticationToken(token, null, Collections.emptyList());
        SecurityContext securityContext = new SecurityContextImpl(authentication);

        exchange.getAttributes().put("token", token);
        when(jwtAuthenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authentication));

        // Act
        Mono<SecurityContext> result = securityContextRepository.load(exchange);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(context -> {
                    assertNotNull(context.getAuthentication());
                    assertTrue(context.getAuthentication().isAuthenticated());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldNotLoadSecurityContextWithNoToken() {
        String token = exchange.getAttribute("token"); // This will be null

        when(jwtAuthenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.empty());

        Mono<SecurityContext> result = securityContextRepository.load(exchange);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldSaveSecurityContextButReturnEmptyMono() {
        // Arrange
        SecurityContext context = new SecurityContextImpl();

        // Act
        Mono<Void> result = securityContextRepository.save(exchange, context);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }
}
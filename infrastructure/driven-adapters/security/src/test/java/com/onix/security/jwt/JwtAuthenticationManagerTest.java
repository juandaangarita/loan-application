package com.onix.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


import com.onix.security.exception.InvalidCredentialsException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationManagerTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtAuthenticationManager jwtAuthenticationManager;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";

    @Test
    void shouldAuthenticateSuccessfullyWithValidToken() {
        // Arrange
        String username = "testuser";
        String role = "ROLE_USER";

        // Create a mock Authentication object with the token
        Authentication authentication = new UsernamePasswordAuthenticationToken(VALID_TOKEN, VALID_TOKEN);

        // Create mock claims
        Claims claims = Jwts.claims()
                .subject(username)
                .add("roles", List.of(Map.of("authority", role)))
                .build();

        // Mock the JwtProvider to return the claims
        when(jwtProvider.getClaims(VALID_TOKEN)).thenReturn(claims);

        // Act
        Mono<Authentication> result = jwtAuthenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(auth -> {
                    assertEquals(username, auth.getName());
                    assertEquals(1, auth.getAuthorities().size());
                    assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority(role)));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void shouldFailAuthenticationWithInvalidToken() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(INVALID_TOKEN, INVALID_TOKEN);

        // Mock the JwtProvider to throw an exception for a bad token
        when(jwtProvider.getClaims(INVALID_TOKEN)).thenThrow(new IllegalArgumentException("Invalid token"));

        // Act
        Mono<Authentication> result = jwtAuthenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof InvalidCredentialsException &&
                        e.getMessage().equals("Invalid credentials. Invalid token"))
                .verify();
    }
}
package com.onix.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import com.onix.security.config.JwtConfigProperties;
import com.onix.security.exception.InvalidCredentialsException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private JwtConfigProperties jwtConfigProperties;

    @InjectMocks
    private JwtProvider jwtProvider;

    private static final String SECRET_KEY = "a_super_secret_key_for_testing_purposes_with_at_least_32_chars";
    private static final String EMAIL = "test@mail.com";
    private static final String ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        lenient().when(jwtConfigProperties.secretKey()).thenReturn(SECRET_KEY);
        lenient().when(jwtConfigProperties.expiration()).thenReturn(3600000); // 1 hour
    }

    @Test
    void shouldGenerateValidToken() {
        // Arrange
        String token = jwtProvider.generateToken(EMAIL, ROLE);
        assertNotNull(token);
    }

    @Test
    void shouldGetClaimsFromValidToken() {
        // Arrange
        String token = jwtProvider.generateToken(EMAIL, ROLE);

        // Act
        Claims claims = jwtProvider.getClaims(token);

        // Assert
        assertEquals(EMAIL, claims.getSubject());
        assertEquals(List.of(Map.of("authority", ROLE)), claims.get("roles"));
    }

    @Test
    void shouldGetSubjectFromValidToken() {
        // Arrange
        String token = jwtProvider.generateToken(EMAIL, ROLE);

        // Act
        String subject = jwtProvider.getSubject(token);

        // Assert
        assertEquals(EMAIL, subject);
    }

    @Test
    void shouldValidateValidToken() {
        // Arrange
        String token = jwtProvider.generateToken(EMAIL, ROLE);

        // Act & Assert
        assertTrue(jwtProvider.validate(token));
    }

    @Test
    void shouldFailValidationForExpiredToken() {
        // Arrange
        String expiredToken = Jwts.builder()
                .subject(EMAIL)
                .claim("roles", List.of(Map.of("authority", ROLE)))
                .issuedAt(Date.from(Instant.now().minus(2, ChronoUnit.HOURS)))
                .expiration(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .compact();

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> jwtProvider.validate(expiredToken));
        assertEquals("Invalid credentials. Token expired", exception.getMessage());
    }

    @Test
    void shouldFailValidationForBadSignature() {
        // Arrange
        String anotherSecretKey = "another_different_secret_key_to_cause_a_bad_signature";
        String badToken = Jwts.builder()
                .subject(EMAIL)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtConfigProperties.expiration()))
                .signWith(Keys.hmacShaKeyFor(anotherSecretKey.getBytes()))
                .compact();

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> jwtProvider.validate(badToken));
        assertEquals("Invalid credentials. Bad signature", exception.getMessage());
    }

    @Test
    void shouldFailValidationForMalformedToken() {
        // Arrange
        String malformedToken = "not.a.valid.jwt";

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> jwtProvider.validate(malformedToken));
        assertEquals("Invalid credentials. Token malformed", exception.getMessage());
    }
}
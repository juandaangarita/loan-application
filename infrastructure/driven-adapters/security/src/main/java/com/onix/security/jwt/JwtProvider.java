package com.onix.security.jwt;

import com.onix.security.config.JwtConfigProperties;
import com.onix.security.exception.InvalidCredentialsException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    private final JwtConfigProperties jwtConfigProperties;

    public String generateToken(String email, String role) {
        Map<String, String> authority = Map.of("authority", role);
        return Jwts.builder()
                .subject(email)
                .claim("roles", List.of(authority))
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtConfigProperties.expiration()))
                .signWith(getKey(jwtConfigProperties.secretKey()))
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey(jwtConfigProperties.secretKey()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(getKey(jwtConfigProperties.secretKey()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validate(String token){
        try {
            Jwts.parser()
                    .verifyWith(getKey(jwtConfigProperties.secretKey()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return true;
        } catch (ExpiredJwtException e) {
            log.error("token expired");
            throw new InvalidCredentialsException("Token expired");
        } catch (UnsupportedJwtException e) {
            log.error("token unsupported");
            throw new InvalidCredentialsException("Token unsupported");
        } catch (MalformedJwtException e) {
            log.error("token malformed");
            throw new InvalidCredentialsException("Token malformed");
        } catch (SignatureException e) {
            log.error("bad signature");
            throw new InvalidCredentialsException("Bad signature");
        } catch (IllegalArgumentException e) {
            log.error("illegal args");
            throw new InvalidCredentialsException("Illegal arguments");
        }
    }

    private SecretKey getKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

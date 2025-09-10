package com.onix.security.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onix.security.exception.InvalidCredentialsException;
import com.onix.security.exception.UnauthorizedClientException;
import com.onix.shared.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class JwtFilter implements WebFilter {

    private static final List<String> WHITELIST = List.of(
            "/api/v1/login",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/webjars",
            "/favicon.ico"
    );

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        if(WHITELIST.stream().anyMatch(path::startsWith))
            return chain.filter(exchange);
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if(auth == null)
            throw new InvalidCredentialsException("No token was found");
        if(!auth.startsWith("Bearer "))
            throw new InvalidCredentialsException("Invalid auth");
        String token = auth.replace("Bearer ", "");
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange)
                .onErrorResume(throwable -> handleException(exchange, throwable));
    }

    private Mono<Void> handleException(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        ApiResponse<Object> body;

        switch (ex) {
            case InvalidCredentialsException invalidCredentialsException -> {
                log.debug(invalidCredentialsException.getMessage());
                status = HttpStatus.UNAUTHORIZED;
                body = ApiResponse.error(status.value(), "Unauthorized", ex.getMessage());
            }
            case UnauthorizedClientException unauthorizedClientException -> {
                log.debug(unauthorizedClientException.getMessage());
                status = HttpStatus.UNAUTHORIZED;
                body = ApiResponse.error(status.value(), "Unauthorized", ex.getMessage());
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
}

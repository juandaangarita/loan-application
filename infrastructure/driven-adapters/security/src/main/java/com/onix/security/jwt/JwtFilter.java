package com.onix.security.jwt;


import com.onix.security.exception.InvalidCredentialsException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
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
        return chain.filter(exchange);
    }
}

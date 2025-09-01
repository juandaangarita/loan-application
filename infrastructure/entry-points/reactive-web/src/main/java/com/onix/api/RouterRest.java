package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.api.openapi.LoanOpenApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final LoanConfig loanConfig;
    private final LoanHandler loanHandler;

    @Bean
    public RouterFunction<ServerResponse> routerFunction(LoanHandler handler) {
        return route()
                .POST(loanConfig.getLoan(), loanHandler::listenSaveLoan, LoanOpenApi::createLoan)
        .build();
    }
}

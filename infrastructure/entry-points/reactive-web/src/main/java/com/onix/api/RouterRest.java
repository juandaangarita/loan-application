package com.onix.api;

import com.onix.api.config.LoanConfig;
import com.onix.model.loanapplication.Loan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final LoanConfig loanConfig;
    private final LoanHandler loanHandler;

    @Bean
    @RouterOperation(
            path = "/api/v1/loan",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE},
            beanClass = LoanHandler.class,
            beanMethod = "listenSaveLoan",
            operation = @Operation(
                    operationId = "submitLoan",
                    summary = "Submit a new loan",
                    description = "Submit a loan in the system",
                    requestBody = @RequestBody(
                            required = true,
                            content = @Content(schema = @Schema(implementation = Loan.class))
                    ),
                    responses = {
                            @ApiResponse(responseCode = "201", description = "Loan created successfully", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "409", description = "Conflict error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class))),
                            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = com.onix.api.dto.ApiResponse.class)))
                    }
            )
    )
    public RouterFunction<ServerResponse> routerFunction(LoanHandler loanHandler) {
        return route(POST(loanConfig.getLoan()), loanHandler::listenSaveLoan);
    }
}

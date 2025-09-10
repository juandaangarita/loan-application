package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.api.dto.CreateLoanDTO;
import com.onix.api.dto.LoanDTO;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class LoanOpenApi {

    public void createLoan(Builder builder) {
        var successResponse = new LoanDTO(
                UUID.randomUUID(),
                BigDecimal.valueOf(9000),
                12,
                "email@email.com",
                "1234567890",
                1,
                1);

        var requestExample = new CreateLoanDTO(
                BigDecimal.valueOf(9000),
                12,
                "email@email.com",
                "1234567890",
                1
        );

        builder
                .operationId("submitLoan")
                .summary("Submit a new loan")
                .description("Submit a loan in the system")
                .tag("Loan")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CreateLoanDTO.class))
                                .example(exampleOjectBuilder()
                                        .value(UtilOpenApi.createObjectToString(requestExample)))))
                .response(UtilOpenApi.responseApiBuilder(201, "Loan created successfully", successResponse))
                .response(UtilOpenApi.responseApiBuilder(400, "Validation error", null))
                .response(UtilOpenApi.responseApiBuilder(409, "Conflict error", null))
                .response(UtilOpenApi.responseApiBuilder(500, "Internal server error", null))
                .response(UtilOpenApi.responseApiBuilder(503, "Service unavailable", null));;
    }
}

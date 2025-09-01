package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.onix.api.dto.ApiResponse;
import com.onix.api.dto.CreateLoanDTO;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;

@UtilityClass
public class LoanOpenApi {

    public void createLoan(Builder builder) {
        var jsonContent = contentBuilder()
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ApiResponse.class));

        builder
                .operationId("submitLoan")
                .summary("Submit a new loan")
                .description("Submit a loan in the system")
                .tag("Loan")
                .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                                .schema(schemaBuilder().implementation(CreateLoanDTO.class))))
                .response(responseBuilder()
                        .responseCode("201").description("Loan created successfully")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("400").description("Validation error")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("409").description("Conflict error")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("500").description("Internal server error")
                        .content(jsonContent))
                .response(responseBuilder()
                        .responseCode("503").description("Service unavailable")
                        .content(jsonContent));
    }
}

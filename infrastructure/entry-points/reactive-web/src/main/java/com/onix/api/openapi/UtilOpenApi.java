package com.onix.api.openapi;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.exampleobject.Builder.exampleOjectBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onix.shared.dto.ApiResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;

@UtilityClass
public class UtilOpenApi {

    ObjectMapper mapper = new ObjectMapper();

    public org.springdoc.core.fn.builders.apiresponse.Builder responseApiBuilder(int status, String error, Object data) {
        var example = exampleOjectBuilder()
                .value(createObjectToString(createApiResponse(status, error, data)));

        var jsonContent = contentBuilder()
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .schema(schemaBuilder().implementation(ApiResponse.class))
                .example(example);

        return responseBuilder()
                .responseCode(String.valueOf(status)).description(error)
                .content(jsonContent);
    }

    public String createObjectToString(Object response) {
        try {
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> ApiResponse<T> createApiResponse(int status, String error, T data) {
        if (status >= 200 && status < 300) {
            return ApiResponse.success(status, error, data);
        }
        return ApiResponse.error(status, error, error);
    }


}

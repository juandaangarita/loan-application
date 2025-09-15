package com.onix.api.config;

import com.onix.usecase.loanapplication.validator.LoanValidator;
import lombok.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Generated
@Configuration
public class ValidatorConfig {

    @Bean
    public LoanValidator userValidator() {
        return new LoanValidator();
    }
}

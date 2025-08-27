package com.onix.api.config;

import com.onix.usecase.loanapplication.validator.LoanValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public LoanValidator userValidator() {
        return new LoanValidator();
    }
}

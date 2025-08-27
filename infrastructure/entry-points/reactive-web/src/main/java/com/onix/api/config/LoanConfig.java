package com.onix.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.paths")
public class LoanConfig {
    private String base;
    private String loan;

    public String getLoan() {
        return base + loan;
    }
}

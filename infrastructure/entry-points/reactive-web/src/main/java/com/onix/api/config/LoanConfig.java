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
    private String type;
    private String status;

    public String getLoan() {
        return base + loan;
    }

    public String getType() {
        return base + type;
    }

    public String getStatus() {
        return base + status;
    }
}

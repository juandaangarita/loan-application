package com.onix.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "adapter.user")
public class UserPropertiesConfig {
    private String baseUrl;
    private String validateUserPath;
    private int timeout;

}

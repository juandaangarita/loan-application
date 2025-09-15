package com.onix.security.config;

import lombok.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtConfigProperties (
        String secretKey,
        Integer expiration,
        String defaultRole,
        String defaultAdminRole,
        String tokenPrefix,
        String defaultPassword) {
}

package com.onix.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String statusQueueUrl,
     String creationQueueUrl,
     String endpoint){
}

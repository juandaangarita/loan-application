package com.onix.consumer.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Configuration
@RequiredArgsConstructor
public class RestConsumerConfig {

    private final UserPropertiesConfig userPropertiesConfig;

    @Bean
    public WebClient userWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl(userPropertiesConfig.getBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .clientConnector(getClientHttpConnector())
            .build();
    }

    private ClientHttpConnector getClientHttpConnector() {
        return new ReactorClientHttpConnector(HttpClient.create()
                .compress(true)
                .keepAlive(true)
                .option(CONNECT_TIMEOUT_MILLIS, userPropertiesConfig.getTimeout())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(userPropertiesConfig.getTimeout(), MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(userPropertiesConfig.getTimeout(), MILLISECONDS));
                }));
    }

}

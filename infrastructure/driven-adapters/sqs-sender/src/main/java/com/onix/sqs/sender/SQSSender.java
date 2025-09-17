package com.onix.sqs.sender;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.gateways.LoanStatusPublisher;
import com.onix.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements LoanStatusPublisher {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    @Override
    public Mono<Void> sendStatusUpdate(Loan loan, String status, String username) {
        String message = """
            {
                "loanId": "%s",
                "username": "%s",
                "email": "%s",
                "status": "%s"
            }
            """.formatted(loan.getLoanId(), username, loan.getEmail(), status);

        SendMessageRequest request = buildRequest(message);

        log.trace("Publishing loan status to SQS: {}", message);

        return Mono.fromFuture(() -> client.sendMessage(request))
                .doOnSuccess(resp -> log.debug("SQS message sent, id={}", resp.messageId()))
                .doOnError(err -> log.error("Error sending SQS message", err))
                .then();
    }
}

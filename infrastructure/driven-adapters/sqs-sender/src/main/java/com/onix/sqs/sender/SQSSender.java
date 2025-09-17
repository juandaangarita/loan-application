package com.onix.sqs.sender;

import com.onix.model.loanapplication.Loan;
import com.onix.model.loanapplication.dto.UserDTO;
import com.onix.model.loanapplication.gateways.LoanPublisher;
import com.onix.sqs.sender.config.SQSSenderProperties;
import java.util.List;
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
public class SQSSender implements LoanPublisher {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    public Mono<String> send(String message, String queueUrl) {
        return Mono.fromCallable(() -> buildRequest(message, queueUrl))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message, String queueUrl) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
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

        SendMessageRequest request = buildRequest(message, properties.statusQueueUrl());

        log.trace("Publishing loan status to SQS: {}", message);

        return Mono.fromFuture(() -> client.sendMessage(request))
                .doOnSuccess(resp -> log.debug("status loan SQS message sent, id={}", resp.messageId()))
                .doOnError(err -> log.error("Error sending status loan SQS message", err))
                .then();
    }

    @Override
    public Mono<Void> sendCreationEvent(Loan newloan, List<Loan> approvedLoans, UserDTO user) {
        String message = """
            {
                "newLoan": "%s",
                "approvedLoans": "%s",
                "userInfo": "%s"
            }
            """.formatted(newloan, approvedLoans, user);

        SendMessageRequest request = buildRequest(message, properties.creationQueueUrl());

        log.trace("Publishing loan creation to SQS: {}", message);

        return Mono.fromFuture(() -> client.sendMessage(request))
                .doOnSuccess(resp -> log.debug("creation loan SQS message sent, id={}", resp.messageId()))
                .doOnError(err -> log.error("Error sending creation loan SQS message", err))
                .then();
    }
}

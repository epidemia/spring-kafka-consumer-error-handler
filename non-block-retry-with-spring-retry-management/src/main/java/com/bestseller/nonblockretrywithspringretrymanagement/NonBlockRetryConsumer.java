package com.bestseller.nonblockretrywithspringretrymanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Not block retry solution.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NonBlockRetryConsumer {

    private final Handler handler;

    @RetryableTopic(
        attempts = "3",
        autoCreateTopics = "true",
        backoff = @Backoff(delay = 1000, maxDelay = 10000, multiplier = 2),
        include = Exception.class
    )
    @KafkaListener(topics = "NonBlockRetry", groupId = "GroupId")
    public void consume(String payload) {
        log.info("Consuming NotBlockRetry: payload={}", payload);
        handler.handle(payload);
        log.info("NotBlockRetry successfully consumed: payload={}", payload);
    }

    @DltHandler
    void process(String payload) {
        log.info("Processing DLT: payload={}", payload);
    }

}

package com.bestseller.nonblockretrywithspringretrymanagement;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NonBlockRetryConsumerTest extends AbstractIntegrationTest {

    private final static String TOPIC = "NonBlockRetry";

    @Autowired
    private KafkaProducer<String, Object> kafkaProducer;

    @Autowired
    private Handler mockHandler;

    @Test
    void success_message() {
        // arrange
        var payload = UUID.randomUUID().toString();
        doNothing().when(mockHandler).handle(payload);

        // act
        kafkaProducer.send(new ProducerRecord<>(TOPIC, payload));

        // assert
        await().untilAsserted(() -> verify(mockHandler, times(1)).handle(payload));
    }

    @Test
    void failed_message() {
        // arrange
        var errorPayload = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(mockHandler).handle(errorPayload);
        kafkaProducer.send(new ProducerRecord<>(TOPIC, errorPayload));

        // assert
        await()
            .atMost(Duration.ofMinutes(1))
            .untilAsserted(() -> verify(mockHandler, times(3)).handle(errorPayload));
    }

}

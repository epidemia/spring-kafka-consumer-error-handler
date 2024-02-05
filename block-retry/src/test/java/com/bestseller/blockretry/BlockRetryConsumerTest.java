package com.bestseller.blockretry;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class BlockRetryConsumerTest extends AbstractIntegrationTest {

    @Value("${spring.cloud.stream.bindings.blockRetryConsumer-in-0.destination}")
    private String topic;

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
        kafkaProducer.send(new ProducerRecord<>(topic, payload));

        // assert
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> verify(mockHandler, Mockito.times(1)).handle(payload));
    }

    @Test
    void failed_message() {
        // arrange
        var payload = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(mockHandler).handle(payload);

        // act
        kafkaProducer.send(new ProducerRecord<>(topic, payload));

        // assert
        await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> verify(mockHandler, Mockito.times(4)).handle(payload));
    }
}

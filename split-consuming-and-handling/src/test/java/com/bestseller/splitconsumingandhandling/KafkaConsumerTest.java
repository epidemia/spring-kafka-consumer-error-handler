package com.bestseller.splitconsumingandhandling;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KafkaConsumerTest extends AbstractIntegrationTest {

    @Value("${spring.cloud.stream.bindings.kafkaConsumer-in-0.destination}")
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
        await().untilAsserted(() -> verify(mockHandler, times(1)).handle(payload));
    }

    @Test
    void failed_message() {
        // arrange
        var payload = UUID.randomUUID().toString();
        doThrow(RuntimeException.class)
            .doThrow(RuntimeException.class)
            .doThrow(RuntimeException.class)
            .doThrow(RuntimeException.class)
            .doNothing()
            .when(mockHandler).handle(payload);

        // act
        kafkaProducer.send(new ProducerRecord<>(topic, payload));

        // assert
        await()
            .atMost(Duration.ofSeconds(30))
            .untilAsserted(() -> verify(mockHandler, times(5)).handle(payload));
    }

}

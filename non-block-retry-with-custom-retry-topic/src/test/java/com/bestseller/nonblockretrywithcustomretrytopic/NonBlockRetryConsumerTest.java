package com.bestseller.nonblockretrywithcustomretrytopic;

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

public class NonBlockRetryConsumerTest extends AbstractIntegrationTest {

    @Value("${spring.cloud.stream.bindings.nonBlockRetryConsumer-in-0.destination}")
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
        var errorPayload = UUID.randomUUID().toString();
        doThrow(RuntimeException.class).when(mockHandler).handle(errorPayload);
        kafkaProducer.send(new ProducerRecord<>(topic, errorPayload));

        var successPayload1 = UUID.randomUUID().toString();
        doNothing().when(mockHandler).handle(successPayload1);
        kafkaProducer.send(new ProducerRecord<>(topic, successPayload1));

        var successPayload2 = UUID.randomUUID().toString();
        doNothing().when(mockHandler).handle(successPayload2);
        kafkaProducer.send(new ProducerRecord<>(topic, successPayload2));

        // assert
        await()
            .atMost(Duration.ofMinutes(1))
            .untilAsserted(() -> {
                verify(mockHandler, times(1)).handle(successPayload1);
                verify(mockHandler, times(1)).handle(successPayload2);
                verify(mockHandler, times(5)).handle(errorPayload);
            });
    }

}

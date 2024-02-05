package com.bestseller.nonblockretrywithspringretrymanagement;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

@TestConfiguration
@Slf4j
public class IntegrationTestConfiguration {

    @Bean
    public KafkaProducer<String, Object> kafkaProducer(
        @Value("${spring.kafka.bootstrap-servers}") String kafkaBrokerList) {
        log.info("Kafka broker list: {}", kafkaBrokerList);
        return new KafkaProducer<>(
            Map.of(
                BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerList,
                KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class,
                VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
            )
        );
    }

    @Bean
    @Primary
    public Handler mockHandler() {
        return Mockito.mock(Handler.class);
    }

}

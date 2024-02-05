package com.bestseller.splitconsumingandhandling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;

import java.util.function.Consumer;

/**
 * Not block retry solution.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer implements Consumer<String> {

    private final QueueProducer<String> queueTaskProducer;

    @Override
    public void accept(String s) {
        log.info("Consuming message: payload={}", s);
        queueTaskProducer.enqueue(EnqueueParams.create(s));
        log.info("Message successfully consumed: payload={}", s);
    }

}

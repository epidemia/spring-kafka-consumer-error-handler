package com.bestseller.nonblockretrywithcustomretrytopic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Not block retry solution.
 */
@Component
@Slf4j
public class NonBlockRetryConsumer implements Consumer<String> {

    private static final String RETRY_TOPIC = "retryTopicProducer-out-0";

    private final Handler handler;

    private final StreamBridge streamBridge;

    public NonBlockRetryConsumer(Handler handler, StreamBridge streamBridge) {
        this.handler = handler;
        this.streamBridge = streamBridge;
    }

    @Override
    public void accept(String s) {
        log.info("Consuming NotBlockRetry: payload={}", s);
        try {
            handler.handle(s);
        } catch (Exception e) {
            log.error("Error occurred while handling NotBlockRetry message: payload={}", s, e);
            streamBridge.send(RETRY_TOPIC, s);
            log.info("NotBlockRetry message sent to RetryTopic: payload={}", s);
            return;
        }
        log.info("NotBlockRetry successfully consumed: payload={}", s);
    }

}

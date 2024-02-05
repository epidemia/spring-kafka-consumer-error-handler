package com.bestseller.nonblockretrywithcustomretrytopic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class RetryTopicConsumer implements Consumer<String> {

    private final Handler handler;

    public RetryTopicConsumer(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void accept(String s) {
        log.info("Consuming RetryTopic: payload={}", s);
        handler.handle(s);
        log.info("RetryTopic successfully consumed: payload={}", s);
    }

}

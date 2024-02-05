package com.bestseller.blockretry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Block retry solution.
 */
@Component
@Slf4j
public class BlockRetryConsumer implements Consumer<String> {

    private final Handler handler;

    public BlockRetryConsumer(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void accept(String s) {
        log.info("Consuming BlockRetry: payload={}", s);
        handler.handle(s);
        log.info("BlockRetry successfully consumed: payload={}", s);
    }

}

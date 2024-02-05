package com.bestseller.nonblockingretrywithoutretrytopic;

import org.springframework.stereotype.Service;

@Service
public class DummyHandler implements Handler {

    @Override
    public void handle(String message) {
    }
}

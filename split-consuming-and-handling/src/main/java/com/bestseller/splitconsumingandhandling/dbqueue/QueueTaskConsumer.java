package com.bestseller.splitconsumingandhandling.dbqueue;

import com.bestseller.splitconsumingandhandling.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.api.Task;
import ru.yoomoney.tech.dbqueue.api.TaskExecutionResult;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;

@RequiredArgsConstructor
@Slf4j
public class QueueTaskConsumer implements QueueConsumer<String> {

    private final Handler handler;

    private final QueueConfig queueConfig;

    private final TaskPayloadTransformer<String> taskPayloadTransformer;

    @Override
    public TaskExecutionResult execute(Task<String> task) {
        log.info("Consuming message: payload={}", task.getPayloadOrThrow());
        handler.handle(task.getPayloadOrThrow());
        return TaskExecutionResult.finish();
    }

    @Override
    public QueueConfig getQueueConfig() {
        return queueConfig;
    }

    @Override
    public TaskPayloadTransformer<String> getPayloadTransformer() {
        return taskPayloadTransformer;
    }

}

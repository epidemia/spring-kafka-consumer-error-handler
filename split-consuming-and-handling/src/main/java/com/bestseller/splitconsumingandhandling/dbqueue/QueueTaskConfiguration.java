package com.bestseller.splitconsumingandhandling.dbqueue;

import com.bestseller.splitconsumingandhandling.Handler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;
import ru.yoomoney.tech.dbqueue.api.impl.NoopPayloadTransformer;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.api.impl.SingleQueueShardRouter;
import ru.yoomoney.tech.dbqueue.config.DatabaseAccessLayer;
import ru.yoomoney.tech.dbqueue.config.DatabaseDialect;
import ru.yoomoney.tech.dbqueue.config.QueueService;
import ru.yoomoney.tech.dbqueue.config.QueueShard;
import ru.yoomoney.tech.dbqueue.config.QueueShardId;
import ru.yoomoney.tech.dbqueue.config.QueueTableSchema;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingTaskLifecycleListener;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingThreadLifecycleListener;
import ru.yoomoney.tech.dbqueue.settings.ExtSettings;
import ru.yoomoney.tech.dbqueue.settings.FailRetryType;
import ru.yoomoney.tech.dbqueue.settings.FailureSettings;
import ru.yoomoney.tech.dbqueue.settings.PollSettings;
import ru.yoomoney.tech.dbqueue.settings.ProcessingMode;
import ru.yoomoney.tech.dbqueue.settings.ProcessingSettings;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;
import ru.yoomoney.tech.dbqueue.settings.QueueId;
import ru.yoomoney.tech.dbqueue.settings.QueueLocation;
import ru.yoomoney.tech.dbqueue.settings.QueueSettings;
import ru.yoomoney.tech.dbqueue.settings.ReenqueueRetryType;
import ru.yoomoney.tech.dbqueue.settings.ReenqueueSettings;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;

@Configuration
public class QueueTaskConfiguration {

    @Bean
    public QueueConfig queueConfig() {
        return new QueueConfig(
            QueueLocation.builder()
                .withQueueId(new QueueId("simple-queue"))
                .withTableName("queue_tasks")
                .build(),
            QueueSettings.builder()
                .withProcessingSettings(ProcessingSettings.builder()
                    .withProcessingMode(ProcessingMode.SEPARATE_TRANSACTIONS)
                    .withThreadCount(1).build())
                .withPollSettings(PollSettings.builder()
                    .withBetweenTaskTimeout(Duration.ofMillis(100))
                    .withNoTaskTimeout(Duration.ofMillis(100))
                    .withFatalCrashTimeout(Duration.ofSeconds(1)).build())
                .withFailureSettings(FailureSettings.builder()
                    .withRetryType(FailRetryType.LINEAR_BACKOFF)
                    .withRetryInterval(Duration.ofSeconds(1)).build())
                .withReenqueueSettings(ReenqueueSettings.builder()
                    .withRetryType(ReenqueueRetryType.MANUAL).build())
                .withExtSettings(ExtSettings.builder().withSettings(new LinkedHashMap<>()).build())
                .build()
        );
    }

    @Bean
    public TaskPayloadTransformer<String> taskPayloadTransformer() {
        return NoopPayloadTransformer.getInstance();
    }

    @Bean
    public DataSource dataSource(
        @Value("${spring.datasource.url}") String url,
        @Value("${spring.datasource.username}") String username,
        @Value("${spring.datasource.password}") String password
    ) {
        return new DriverManagerDataSource(url, username, password);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public SpringDatabaseAccessLayer databaseAccessLayer(
        JdbcTemplate jdbcTemplate,
        TransactionTemplate transactionTemplate) {
        return new SpringDatabaseAccessLayer(
            DatabaseDialect.H2,
            QueueTableSchema.builder().build(),
            jdbcTemplate,
            transactionTemplate
        );
    }

    @Bean
    public QueueShard<DatabaseAccessLayer> queueShard(SpringDatabaseAccessLayer databaseAccessLayer) {
        return new QueueShard<>(new QueueShardId("main"), databaseAccessLayer);
    }

    @Bean
    public QueueProducer<String> queueTaskProducer(QueueConfig queueConfig,
                                                   TaskPayloadTransformer<String> taskPayloadTransformer,
                                                   QueueShard<DatabaseAccessLayer> queueShard) {
        return new ShardingQueueProducer<>(
            queueConfig,
            taskPayloadTransformer,
            new SingleQueueShardRouter<>(queueShard)
        );
    }

    @Bean
    public QueueTaskConsumer queueTaskConsumer(
        Handler handler,
        QueueConfig queueConfig,
        TaskPayloadTransformer<String> taskPayloadTransformer) {
        return new QueueTaskConsumer(handler, queueConfig, taskPayloadTransformer);
    }

    @Bean
    public QueueService queueService(QueueShard queueShard, QueueConsumer<String> queueConsumer) {
        QueueService queueService = new QueueService(
            List.of(queueShard),
            new LoggingThreadLifecycleListener(),
            new LoggingTaskLifecycleListener()
        );
        queueService.registerQueue(queueConsumer);
        queueService.start();
        return queueService;
    }

}

package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import java.time.Duration;
import java.time.Instant;
import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.logging.DataMapHolder;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

/**
 * Consumes messages from the configured main Kafka topic.
 */
@Component
public class Consumer {

    private final Service service;
    private final Logger logger;

    public Consumer(Service service, Logger logger) {
        this.service = service;
        this.logger = logger;
    }

    @RetryableTopic(
            attempts = "${consumer.max_attempts}",
            autoCreateTopics = "false",
            backOff = @BackOff(delayString = "${consumer.backoff_delay}"),
            retryTopicSuffix = "-${consumer.group_id}-retry",
            dltTopicSuffix = "-${consumer.group_id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            exclude = NonRetryableException.class
    )
    @KafkaListener(
            id = "${consumer.topic}-consumer",
            topics = "${consumer.topic}",
            groupId = "${consumer.group_id}",
            autoStartup = "true",
            containerFactory = "listenerContainerFactory"
    )
    public void consume(Message<@NonNull ResourceChangedData> message) {
        logger.info("consume(kind=%s) method called.".formatted(
                message.getPayload().getResourceKind()), DataMapHolder.getLogMap());

        Instant startTime = Instant.now();

        ResourceChangedData payload = message.getPayload();
        String contextId = payload.getContextId();

        try {
            service.processMessage(new ServiceParameters(message.getPayload()));

            long messageProcessingTime = Duration.between(startTime, Instant.now()).toMillis();

            logger.info("Message Processed [%s]: %d milliseconds".formatted(payload.getResourceKind(),
                    messageProcessingTime), DataMapHolder.getLogMap());

        } catch (Exception exception) {
            logger.errorContext(contextId, "Exception occurred while processing message",
                    exception, DataMapHolder.getLogMap());

            throw exception;
        }
    }
}

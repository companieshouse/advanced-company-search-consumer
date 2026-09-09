package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class MessageLoggingAspectTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private JoinPoint joinPoint;

    private MessageLoggingAspect aspect;

    @BeforeAll
    static void captureOriginalLogger() throws Exception {
    }

    @BeforeEach
    void injectMockLogger() throws Exception {
        aspect = new MessageLoggingAspect(mockLogger);
    }

    @AfterEach
    void restoreOriginalLogger() throws Exception {
    }

    private Message<?> messageWithHeaders(String topic, Integer partition, Long offset) {
        return new GenericMessage<>("payload", Map.of(
                KafkaHeaders.RECEIVED_TOPIC, topic,
                KafkaHeaders.RECEIVED_PARTITION, partition,
                KafkaHeaders.OFFSET, offset));
    }

    @Test
    void logBeforeMainConsumer_logsReceivedMessageWithTopicPartitionOffset() {
        Message<?> message = messageWithHeaders("my-topic", 2, 42L);
        org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] {message});

        aspect.logBeforeMainConsumer(joinPoint);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockLogger).debug(eq("Processing delta"), captor.capture());
        Map<String, Object> logged = captor.getValue();
        assertEquals("my-topic", logged.get("topic"));
        assertEquals(2, logged.get("partition"));
        assertEquals(42L, logged.get("offset"));
    }

    @Test
    void logAfterMainConsumer_logsProcessedMessageWithTopicPartitionOffset() {
        Message<?> message = messageWithHeaders("another-topic", 5, 100L);
        org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] {message});

        aspect.logAfterMainConsumer(joinPoint);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockLogger).debug(eq("Processed delta"), captor.capture());
        Map<String, Object> logged = captor.getValue();
        assertEquals("another-topic", logged.get("topic"));
        assertEquals(5, logged.get("partition"));
        assertEquals(100L, logged.get("offset"));
    }

    @Test
    void afterThrowingAdvice_logsFormattedExceptionMessage() {
        Message<?> message = messageWithHeaders("err-topic", 1, 7L);
        org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] {message});
        Throwable error = new IllegalStateException("boom");

        aspect.afterThrowingAdvice(joinPoint, error);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockLogger).debug(eq("IllegalStateException exception thrown: boom"), captor.capture());
        Map<String, Object> logged = captor.getValue();
        assertEquals("err-topic", logged.get("topic"));
        assertEquals(1, logged.get("partition"));
        assertEquals(7L, logged.get("offset"));
    }

    @Test
    void logMessage_defaultsToPlaceholdersWhenHeadersAreMissing() {
        Message<?> message = new GenericMessage<>("payload", Collections.emptyMap());
        org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] {message});

        aspect.logBeforeMainConsumer(joinPoint);

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockLogger).debug(eq("Processing delta"), captor.capture());
        Map<String, Object> logged = captor.getValue();
        assertEquals("no topic", logged.get("topic"));
        assertEquals(0, logged.get("partition"));
        assertEquals(0L, logged.get("offset"));
    }
}
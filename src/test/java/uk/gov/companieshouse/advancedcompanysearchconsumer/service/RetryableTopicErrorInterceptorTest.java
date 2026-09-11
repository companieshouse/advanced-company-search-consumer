package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE;

import consumer.exception.NonRetryableErrorException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryableTopicErrorInterceptorTest {

    private RetryableTopicErrorInterceptor underTest;

    @BeforeEach
    void setUp() {
        underTest = new RetryableTopicErrorInterceptor();
    }

    @Test
    void onSend_shouldReturnOriginalMessage_whenTopicIsNotErrorTopic() {
        ProducerRecord<String, Object> message =
                new ProducerRecord<>("company-search", "key", "value");

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertSame(message, result);
    }

    @Test
    void onSend_shouldReturnInvalidTopic_whenCauseHeaderContainsNonRetryableException() {
        RecordHeaders headers = new RecordHeaders();
        headers.add(EXCEPTION_CAUSE_FQCN, NonRetryableErrorException.class.getName().getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message = new ProducerRecord<>("company-search-error",
                        null,"key","value", headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertNotSame(message, result);
        assertEquals("company-search-invalid", result.topic());
        assertEquals("key", result.key());
        assertEquals("value", result.value());
    }

    @Test
    void onSend_shouldReturnInvalidTopic_whenStackTraceHeaderContainsNonRetryableException() {
        RecordHeaders headers = new RecordHeaders();
        headers.add(EXCEPTION_STACKTRACE, ("some stack trace: "+ NonRetryableErrorException.class.getName()
                        + ": invalid data").getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message = new ProducerRecord<>("company-search-error",
                        null,"key","value", headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertNotSame(message, result);
        assertEquals("company-search-invalid", result.topic());
        assertEquals("key", result.key());
        assertEquals("value", result.value());
    }

    @Test
    void onSend_shouldReturnOriginalMessage_whenNoExceptionHeadersExist() {
        ProducerRecord<String, Object> message =
                new ProducerRecord<>(
                        "company-search-error",
                        "key",
                        "value");

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertSame(message, result);
    }

    @Test
    void onSend_shouldReturnOriginalMessage_whenCauseHeaderDoesNotContainNonRetryableException() {
        RecordHeaders headers = new RecordHeaders();
        headers.add(
                EXCEPTION_CAUSE_FQCN,
                "some.other.Exception".getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message =
                new ProducerRecord<>(
                        "company-search-error",
                        null,
                        "key",
                        "value",
                        headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertSame(message, result);
    }

    @Test
    void onSend_shouldReturnOriginalMessage_whenStackTraceDoesNotContainNonRetryableException() {
        RecordHeaders headers = new RecordHeaders();
        headers.add(
                EXCEPTION_STACKTRACE,
                "some.other.Exception: something went wrong"
                        .getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message =
                new ProducerRecord<>(
                        "company-search-error",
                        null,
                        "key",
                        "value",
                        headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertSame(message, result);
    }

    @Test
    void onSend_shouldReturnInvalidTopic_whenBothHeadersExistAndCauseIsNonRetryable() {
        RecordHeaders headers = new RecordHeaders();

        headers.add(EXCEPTION_CAUSE_FQCN, NonRetryableErrorException.class.getName().getBytes(StandardCharsets.UTF_8));
        headers.add(EXCEPTION_STACKTRACE, "some.other.Exception".getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message = new ProducerRecord<>("company-search-error",
                        null,"key","value", headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertNotSame(message, result);
        assertEquals("company-search-invalid", result.topic());
    }

    @Test
    void onSend_shouldReturnInvalidTopic_whenBothHeadersExistAndStackTraceIsNonRetryable() {
        RecordHeaders headers = new RecordHeaders();

        headers.add(EXCEPTION_CAUSE_FQCN, "some.other.Exception".getBytes(StandardCharsets.UTF_8));
        headers.add(EXCEPTION_STACKTRACE, NonRetryableErrorException.class.getName().getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, Object> message = new ProducerRecord<>("company-search-error",
                        null,"key","value", headers);

        ProducerRecord<String, Object> result = underTest.onSend(message);

        assertNotSame(message, result);
        assertEquals("company-search-invalid", result.topic());
    }

    @Test
    void onAcknowledgement_shouldNotThrowException() {
        RecordMetadata metadata = mock(RecordMetadata.class);
        Exception exception = new RuntimeException("test");

        underTest.onAcknowledgement(metadata, exception);

        verify(metadata, times(1)).topic();
    }

    @Test
    void onAcknowledgement_shouldNotThrowException_whenExceptionIsNull() {
        RecordMetadata metadata = mock(RecordMetadata.class);

        underTest.onAcknowledgement(metadata, null);

        verify(metadata, times(1)).topic();
    }

    @Test
    void close_shouldNotThrowException() {
        assertDoesNotThrow(() -> underTest.close());
    }

    @Test
    void configure_shouldNotThrowException() {
        Map<String, Object> config = Collections.singletonMap("test", "value");

        underTest.configure(config);

        assertThat(config).containsEntry("test", "value");
    }
}

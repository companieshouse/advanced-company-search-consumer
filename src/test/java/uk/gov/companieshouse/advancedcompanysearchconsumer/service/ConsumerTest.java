package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.MessageFlags;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class ConsumerTest {

    @Mock
    private Service service;

    @Mock
    private MessageFlags messageFlags;

    @Mock
    private Message<@NonNull ResourceChangedData> message;

    private ResourceChangedData payload;

    private Consumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new Consumer(service, messageFlags);

        payload = TestConstants.UPDATE;

        when(message.getPayload()).thenReturn(payload);
    }

    @Test
    void shouldProcessMessageSuccessfully() {
        consumer.consume(message);

        verify(service).processMessage(
                argThat(parameters ->
                        parameters.getData() == payload)
        );

        verify(messageFlags, never()).setRetryable(true);
    }

    @Test
    void shouldSetRetryableFlagAndRethrowRetryableException() {
        RetryableException retryableException = new RetryableException("Retryable error", null);

        ServiceParameters parameters = new ServiceParameters(payload);
        doThrow(retryableException).when(service).processMessage(parameters);

        RetryableException thrownException = assertThrows(RetryableException.class,
                () -> consumer.consume(message)
        );

        assertSame(retryableException, thrownException);

        verify(messageFlags).setRetryable(true);
    }

    @Test
    void shouldPropagateNonRetryableException() {
        RuntimeException runtimeException = new RuntimeException("Something went wrong");

        ServiceParameters parameters = new ServiceParameters(payload);
        doThrow(runtimeException).when(service).processMessage(parameters);

        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> consumer.consume(message)
        );

        assertSame(runtimeException, thrownException);

        verify(messageFlags, never()).setRetryable(true);
    }

    @Test
    void shouldPassPayloadToServiceParameters() {
        consumer.consume(message);

        verify(service).processMessage(
                argThat(parameters ->
                        parameters.getData() == payload)
        );
    }

}

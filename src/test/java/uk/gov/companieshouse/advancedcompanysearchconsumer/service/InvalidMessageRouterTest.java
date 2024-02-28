package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.UPDATE;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.MessageFlags;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class InvalidMessageRouterTest {

    private InvalidMessageRouter invalidMessageRouter;

    @Mock
    private MessageFlags flags;

    @BeforeEach
    void setup() {
        invalidMessageRouter = new InvalidMessageRouter();
        invalidMessageRouter.configure(
            Map.of("message.flags", flags, "invalid.message.topic", "invalid"));
    }

    @Test
    void testOnSendRoutesMessageToInvalidMessageTopicIfNonRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ResourceChangedData> message = new ProducerRecord<>("main", "key",
            UPDATE);

        // when
        ProducerRecord<String, ResourceChangedData> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(equalTo(new ProducerRecord<>("invalid", "key", UPDATE))));
    }

    @Test
    void testOnSendRoutesMessageToTargetTopicIfRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ResourceChangedData> message = new ProducerRecord<>("main", "key",
            UPDATE);
        when(flags.isRetryable()).thenReturn(true);

        // when
        ProducerRecord<String, ResourceChangedData> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(sameInstance(message)));
    }

}
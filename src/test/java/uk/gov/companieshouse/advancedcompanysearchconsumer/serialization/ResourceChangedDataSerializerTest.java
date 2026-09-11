package uk.gov.companieshouse.advancedcompanysearchconsumer.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import consumer.exception.NonRetryableErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class ResourceChangedDataSerializerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ResourceChangedDataSerializer underTest;

    @BeforeEach
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void whenSerializedExpectResourceChangedDataBytes() {
        EventRecord eventRecord = new EventRecord();
        eventRecord.setPublishedAt("2022010351");
        eventRecord.setType("charges");
        ResourceChangedData resourceChangedData = new ResourceChangedData("resource_kind","resource_uri","context_id","resource_id","data",eventRecord);
        byte[] result = underTest.serialize("", resourceChangedData);

        assertThat(decodedData(result)).isEqualTo(resourceChangedData);
    }

    @Test
    void whenSerializedReceivesBytesReturnsBytes() {
        byte[] byteExample = "Sample bytes".getBytes();
        byte[] serialize = underTest.serialize("", byteExample);
        assertThat(serialize).isEqualTo(byteExample);
    }

    @Test
    void whenSerializedFailsThrowsNonRetryableError() {
        Object payload = mock(Object.class);
        when(payload.toString()).thenThrow(new RuntimeException());
        assertThrows(NonRetryableErrorException.class, () -> underTest.serialize("", payload));
    }

    private ResourceChangedData decodedData(byte[] resourceChangedData) {
        var deserializer = new ResourceChangedDataDeserializer(logger);
        return deserializer.deserialize("", resourceChangedData);
    }
}

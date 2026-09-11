package uk.gov.companieshouse.advancedcompanysearchconsumer.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
class ResourceChangedDataDeserializerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private ResourceChangedDataDeserializer deserializer;

    @BeforeEach
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void whenDeserializedExpectValidResourceChangedDataObject() {
        EventRecord eventRecord = new EventRecord();
        eventRecord.setPublishedAt("2022010351");
        eventRecord.setType("charges");

        ResourceChangedData resourceChangedData = new ResourceChangedData(
                "resource_kind","resource_uri","context_id",
                "resource_id","data", eventRecord);

        byte[] data = encodedData(resourceChangedData);

        ResourceChangedData deserializedObject = deserializer.deserialize("", data);

        assertThat(deserializedObject).isEqualTo(resourceChangedData);
    }

    @Test
    void whenDeserializedFailsThrowsNonRetryableError() {
        byte[] data = "Invalid message".getBytes();
        assertThrows(NonRetryableErrorException.class, () -> deserializer.deserialize("", data));
    }

    private byte[] encodedData(ResourceChangedData resourceChangedData){
        var serializer = new ResourceChangedDataSerializer(logger);
        return serializer.serialize("", resourceChangedData);
    }
}

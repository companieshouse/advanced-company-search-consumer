package uk.gov.companieshouse.advancedcompanysearchconsumer.serialization;

import consumer.exception.NonRetryableErrorException;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
public class ResourceChangedDataDeserializer implements Deserializer<ResourceChangedData> {

    private final Logger logger;

    public ResourceChangedDataDeserializer(final Logger logger) {
        this.logger = logger;
    }

    /**
     * deserialize.
     */
    @Override
    public ResourceChangedData deserialize(String topic, byte[] data) {
        logger.info("deserialize(topic=%s, bytes=%d) method called.".formatted(topic, data.length));
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ResourceChangedData> reader = new ReflectDatumReader<>(ResourceChangedData.class);

            return reader.read(null, decoder);

        } catch (Exception ex) {
            logger.error("De-Serialization exception converting to Avro schema: ", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(ex);
        }
    }

}

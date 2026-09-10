package uk.gov.companieshouse.advancedcompanysearchconsumer.serialization;

import consumer.exception.NonRetryableErrorException;
import java.nio.charset.StandardCharsets;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.logging.DataMapHolder;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
public class ResourceChangedDataSerializer implements Serializer<Object> {

    private final Logger logger;

    public ResourceChangedDataSerializer(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public byte[] serialize(@NonNull String topic, @NonNull Object payload) {
        logger.info("serialize(topic=%s, class=%s) method called.".formatted(topic, payload.getClass().getSimpleName()));
        try {
            if (payload instanceof byte[] bytes) {
                return bytes;
            }

            if (payload instanceof ResourceChangedData resourceChangedData) {
                DatumWriter<ResourceChangedData> writer = new SpecificDatumWriter<>();
                EncoderFactory encoderFactory = EncoderFactory.get();

                AvroSerializer<ResourceChangedData> avroSerializer = new AvroSerializer<>(writer, encoderFactory);

                return avroSerializer.toBinary(resourceChangedData);
            }

            return payload.toString().getBytes(StandardCharsets.UTF_8);

        } catch (Exception ex) {
            logger.error("Serialization exception while writing to byte array", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Serialization exception while writing to byte array", ex);
        }
    }
}

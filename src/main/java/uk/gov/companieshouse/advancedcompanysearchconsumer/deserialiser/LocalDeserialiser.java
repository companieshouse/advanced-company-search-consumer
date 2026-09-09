package uk.gov.companieshouse.advancedcompanysearchconsumer.deserialiser;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.Application.NAMESPACE;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class LocalDeserialiser implements org.apache.kafka.common.serialization.Deserializer<String> {
    private Charset encoding = StandardCharsets.UTF_8;

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.deserializer.encoding" : "value.deserializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null)
            encodingValue = configs.get("deserializer.encoding");
        if (encodingValue instanceof String) {
            String encodingName = (String) encodingValue;
            try {
                encoding = Charset.forName(encodingName);
            } catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
                throw new SerializationException("Unsupported encoding " + encodingName, e);
            }
        }
    }

    @Override
    public String deserialize(String topic, byte[] data) {
        if (data == null)
            return null;
        else {
            var dataString = new String(data, encoding);
            LOGGER.info("LocalDeserialiser - deserialize(String topic, byte[] data): " + dataString);
            return dataString;
        }
    }

    @Override
    public String deserialize(String topic, Headers headers, ByteBuffer data) {
        if (data == null) {
            return null;
        }

        String dataString;
        if (data.hasArray()) {
            dataString = new String(data.array(), data.position() + data.arrayOffset(), data.remaining(), encoding);
            LOGGER.info("LocalDeserialiser - deserialize(String topic, Headers headers, ByteBuffer data) [1]: " + dataString);
            return dataString;
        }
        dataString = new String(Utils.toArray(data), encoding);
        LOGGER.info("LocalDeserialiser - deserialize(String topic, Headers headers, ByteBuffer data) [2]: " + dataString);
        return dataString;
    }
}

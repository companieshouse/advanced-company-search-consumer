package uk.gov.companieshouse.advancedcompanysearchconsumer.deserialiser;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class LocalDeserialiserTest {

    private static final String TOPIC = "stream-company-profile";
    private static final String ASCII_MESSAGE = "{\"resource_id\":\"12345678\"}";
    // Round-trips differently under UTF-8 and ISO-8859-1, so it proves which charset was used.
    private static final String UNICODE_MESSAGE = "café";

    private LocalDeserialiser deserialiser;

    @BeforeEach
    void setUp() {
        deserialiser = new LocalDeserialiser();
    }

    @Test
    @DisplayName("Deserialises a byte array using the default UTF-8 encoding")
    void deserialiseByteArrayUsesUtf8ByDefault() {
        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);

        assertThat(deserialiser.deserialize(TOPIC, data), is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Returns null when the byte array is null")
    void deserialiseNullByteArrayReturnsNull() {
        assertThat(deserialiser.deserialize(TOPIC, (byte[]) null), is(nullValue()));
    }

    @Test
    @DisplayName("Returns an empty string when the byte array is empty")
    void deserialiseEmptyByteArrayReturnsEmptyString() {
        assertThat(deserialiser.deserialize(TOPIC, new byte[0]), is(equalTo("")));
    }

    @Test
    @DisplayName("Uses value.deserializer.encoding when deserialising a value")
    void configureUsesValueEncodingForValues() {
        deserialiser.configure(Map.of("value.deserializer.encoding", "ISO-8859-1"), false);

        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);

        assertThat(deserialiser.deserialize(TOPIC, data),
            is(equalTo(new String(data, StandardCharsets.ISO_8859_1))));
    }

    @Test
    @DisplayName("Uses key.deserializer.encoding when deserialising a key")
    void configureUsesKeyEncodingForKeys() {
        deserialiser.configure(Map.of("key.deserializer.encoding", "ISO-8859-1"), true);

        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);

        assertThat(deserialiser.deserialize(TOPIC, data),
            is(equalTo(new String(data, StandardCharsets.ISO_8859_1))));
    }

    @Test
    @DisplayName("Ignores key.deserializer.encoding when deserialising a value")
    void configureIgnoresKeyEncodingForValues() {
        deserialiser.configure(Map.of("key.deserializer.encoding", "ISO-8859-1"), false);

        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);

        assertThat(deserialiser.deserialize(TOPIC, data), is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Falls back to deserializer.encoding when no specific encoding is set")
    void configureFallsBackToGenericEncoding() {
        deserialiser.configure(Map.of("deserializer.encoding", "ISO-8859-1"), false);

        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);

        assertThat(deserialiser.deserialize(TOPIC, data),
            is(equalTo(new String(data, StandardCharsets.ISO_8859_1))));
    }

    @Test
    @DisplayName("Prefers the specific encoding over the generic encoding")
    void configurePrefersSpecificEncoding() {
        deserialiser.configure(Map.of(
            "value.deserializer.encoding", "UTF-8",
            "deserializer.encoding", "ISO-8859-1"), false);

        assertThat(deserialiser.deserialize(TOPIC, UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8)),
            is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Retains the default encoding when no encoding properties are supplied")
    void configureWithoutEncodingRetainsDefault() {
        deserialiser.configure(Map.of("some.other.property", "value"), false);

        assertThat(deserialiser.deserialize(TOPIC, UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8)),
            is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Retains the default encoding when the encoding property is not a String")
    void configureWithNonStringEncodingRetainsDefault() {
        deserialiser.configure(Map.of("value.deserializer.encoding", 1234), false);

        assertThat(deserialiser.deserialize(TOPIC, UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8)),
            is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Retains the default encoding when the encoding property is null")
    void configureWithNullEncodingRetainsDefault() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("value.deserializer.encoding", null);
        configs.put("deserializer.encoding", null);

        deserialiser.configure(configs, false);

        assertThat(deserialiser.deserialize(TOPIC, UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8)),
            is(equalTo(UNICODE_MESSAGE)));
    }

    @Test
    @DisplayName("Throws SerializationException for an unsupported charset")
    void configureWithUnsupportedCharsetThrows() {
        Map<String, Object> configs = Map.of("value.deserializer.encoding", "NOT-A-REAL-CHARSET");

        SerializationException exception =
            assertThrows(SerializationException.class, () -> deserialiser.configure(configs, false));

        assertThat(exception.getMessage(), is(equalTo("Unsupported encoding NOT-A-REAL-CHARSET")));
    }

    @Test
    @DisplayName("Throws SerializationException for an illegal charset name")
    void configureWithIllegalCharsetNameThrows() {
        Map<String, Object> configs = Map.of("value.deserializer.encoding", "**illegal**");

        SerializationException exception =
            assertThrows(SerializationException.class, () -> deserialiser.configure(configs, false));

        assertThat(exception.getMessage(), is(equalTo("Unsupported encoding **illegal**")));
    }

    @Test
    @DisplayName("Deserialises a heap ByteBuffer")
    void deserialiseHeapByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(ASCII_MESSAGE.getBytes(StandardCharsets.UTF_8));

        assertThat(deserialiser.deserialize(TOPIC, null, buffer), is(equalTo(ASCII_MESSAGE)));
    }

    @Test
    @DisplayName("Honours position, offset and limit of a sliced heap ByteBuffer")
    void deserialiseSlicedHeapByteBuffer() {
        byte[] framed = ("XX" + ASCII_MESSAGE + "YY").getBytes(StandardCharsets.UTF_8);
        int payloadLength = ASCII_MESSAGE.getBytes(StandardCharsets.UTF_8).length;

        ByteBuffer buffer = ByteBuffer.wrap(framed);
        buffer.position(2).limit(2 + payloadLength);
        ByteBuffer slice = buffer.slice();

        assertThat(deserialiser.deserialize(TOPIC, null, slice), is(equalTo(ASCII_MESSAGE)));
    }

    @Test
    @DisplayName("Deserialises a direct ByteBuffer that has no backing array")
    void deserialiseDirectByteBuffer() {
        byte[] data = ASCII_MESSAGE.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data).flip();

        assertThat(buffer.hasArray(), is(false));
        assertThat(deserialiser.deserialize(TOPIC, null, buffer), is(equalTo(ASCII_MESSAGE)));
    }

    @Test
    @DisplayName("Deserialises a read-only ByteBuffer that has no accessible array")
    void deserialiseReadOnlyByteBuffer() {
        ByteBuffer buffer =
            ByteBuffer.wrap(ASCII_MESSAGE.getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer();

        assertThat(buffer.hasArray(), is(false));
        assertThat(deserialiser.deserialize(TOPIC, null, buffer), is(equalTo(ASCII_MESSAGE)));
    }

    @Test
    @DisplayName("Returns null when the ByteBuffer is null")
    void deserialiseNullByteBufferReturnsNull() {
        assertThat(deserialiser.deserialize(TOPIC, null, (ByteBuffer) null), is(nullValue()));
    }

    @Test
    @DisplayName("Applies the configured encoding to ByteBuffer deserialisation")
    void deserialiseByteBufferUsesConfiguredEncoding() {
        deserialiser.configure(Map.of("value.deserializer.encoding", "ISO-8859-1"), false);

        byte[] data = UNICODE_MESSAGE.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        assertThat(deserialiser.deserialize(TOPIC, null, buffer),
            is(equalTo(new String(data, StandardCharsets.ISO_8859_1))));
    }
}

package uk.gov.companieshouse.advancedcompanysearchconsumer.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    ApplicationConfig underTest;

    @BeforeEach
    void setUp() {
        underTest = new ApplicationConfig();
    }

    @Test
    void testSerializerFactory_isNotNull() {
        SerializerFactory result = underTest.serializerFactory();

        assertThat(result).isNotNull();
    }

    @Test
    void testJsonMapper_isNotNull() {
        JsonMapper result = underTest.jsonMapper();

        assertThat(result).isNotNull();
    }

    @Test
    void testLogger_isNotNull() {
        Logger result = underTest.getLogger();

        assertThat(result).isNotNull();
    }
}

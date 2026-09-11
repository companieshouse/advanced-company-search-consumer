package uk.gov.companieshouse.advancedcompanysearchconsumer.config;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.Application.NAMESPACE;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    @Bean
    SerializerFactory serializerFactory() {
        LOGGER.info("serializerFactory() method called.");

        return new SerializerFactory();
    }

    @Bean
    JsonMapper jsonMapper() {
        LOGGER.info("jsonMapper() method called.");

        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .changeDefaultPropertyInclusion(incl ->
                        incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .build();
    }

    @Bean
    Logger getLogger() {
        LOGGER.info("getLogger() method called.");

        return LoggerFactory.getLogger(NAMESPACE);
    }

}

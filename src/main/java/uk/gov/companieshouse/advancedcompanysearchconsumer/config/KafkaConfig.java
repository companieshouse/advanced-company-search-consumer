package uk.gov.companieshouse.advancedcompanysearchconsumer.config;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.Application.NAMESPACE;

import consumer.deserialization.AvroDeserializer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.service.InvalidMessageRouter;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.MessageFlags;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.service.ServiceResultStatus;
import uk.gov.companieshouse.service.rest.response.ResponseEntityFactory;
import uk.gov.companieshouse.stream.ResourceChangedData;

import uk.gov.companieshouse.advancedcompanysearchconsumer.deserialiser.LocalDeserialiser;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final String bootstrapServers;
    private final String invalidMessageTopic;
    private final Integer concurrency;

    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${invalid_message_topic}") String invalidMessageTopic,
            @Value("${consumer.concurrency}") Integer concurrency) {
        this.bootstrapServers = bootstrapServers;
        this.invalidMessageTopic = invalidMessageTopic;
        this.concurrency = concurrency;
    }

    @Bean
    public ConcurrentMap<ServiceResultStatus, ResponseEntityFactory> responseEntityFactoryMap() {
        LOGGER.info("responseEntityFactoryMap() method called.");

        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConsumerFactory<@NonNull String, ResourceChangedData> consumerFactory() {
        LOGGER.info("consumerFactory() method called.");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, LocalDeserialiser.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        ErrorHandlingDeserializer<@NonNull ResourceChangedData> errorDeserializer = new ErrorHandlingDeserializer<>(
                new AvroDeserializer<>(ResourceChangedData.class));

        return new DefaultKafkaConsumerFactory<>(config, new LocalDeserialiser(), errorDeserializer);
    }

    @Bean
    public ProducerFactory<@NonNull String, ResourceChangedData> producerFactory(final MessageFlags messageFlags,
        final AvroSerializer<ResourceChangedData> serializer) {
        LOGGER.info("producerFactory() method called.");

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, InvalidMessageRouter.class.getName());
        config.put("message.flags", messageFlags);
        config.put("invalid.message.topic", invalidMessageTopic);

        Serializer<ResourceChangedData> customSerializer = (topic, data) -> {
            try {
                return serializer.toBinary(data); //creates a leading space

            } catch (SerializationException e) {
                var dataMap = new DataMap.Builder()
                        .topic(topic)
                        .kafkaMessage(data.toString())
                        .build()
                        .getLogMap();

                final String error = "Caught SerializationException serializing kafka message: " + e.getMessage();
                LOGGER.error(error, dataMap);

                throw new NonRetryableException(error, e);
            }
        };

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), customSerializer);
    }

    @Bean
    @Scope("prototype")
    public AvroSerializer<ResourceChangedData> serializer() {
        LOGGER.info("serializer() method called.");

        return new SerializerFactory().getSpecificRecordSerializer(ResourceChangedData.class);
    }

    @Bean
    public KafkaTemplate<@NonNull String, @NonNull ResourceChangedData> kafkaTemplate(
        ProducerFactory<@NonNull String, ResourceChangedData> producerFactory) {
        LOGGER.info("kafkaTemplate() method called.");

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ResourceChangedData> kafkaListenerContainerFactory(
        ConsumerFactory<@NonNull String, ResourceChangedData> consumerFactory) {
        LOGGER.info("kafkaListenerContainerFactory() method called.");

        ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ResourceChangedData> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
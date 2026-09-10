package uk.gov.companieshouse.advancedcompanysearchconsumer.config;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.Application.NAMESPACE;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableTopicErrorInterceptor;
import uk.gov.companieshouse.advancedcompanysearchconsumer.serialization.ResourceChangedDataDeserializer;
import uk.gov.companieshouse.advancedcompanysearchconsumer.serialization.ResourceChangedDataSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ResourceChangedDataDeserializer deserializer;
    private final ResourceChangedDataSerializer serializer;
    private final String bootstrapServers;
    private final Integer listenerConcurrency;

    /**
     * Kafka Consumer Factory Message.
     */
    public KafkaConfig(ResourceChangedDataDeserializer deserializer,
            ResourceChangedDataSerializer serializer,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${consumer.concurrency}") Integer listenerConcurrency) {
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.bootstrapServers = bootstrapServers;
        this.listenerConcurrency = listenerConcurrency;
    }

    private Map<String, Object> consumerConfigs() {
        LOGGER.info("consumerConfigs(bootstrapServers=%s) method called.".formatted(bootstrapServers));

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ResourceChangedDataDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return props;
    }

    @Bean
    public ConsumerFactory<@NonNull String, ResourceChangedData> consumerFactory() {
        LOGGER.info("consumerFactory() method called.");

        var errorDeserializer = new ErrorHandlingDeserializer<>(deserializer);
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), errorDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ResourceChangedData> listenerContainerFactory() {
        LOGGER.info("listenerContainerFactory() method called.");

        var factory = new ConcurrentKafkaListenerContainerFactory<@NonNull String, @NonNull ResourceChangedData>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(listenerConcurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    @Bean
    public ProducerFactory<@NonNull String, Object> producerFactory() {
        LOGGER.info("producerFactory() method called.");

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ResourceChangedDataSerializer.class);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, RetryableTopicErrorInterceptor.class.getName());

        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), serializer);
    }

    @Bean
    public KafkaTemplate<@NonNull String, @NonNull Object> kafkaTemplate(ProducerFactory<@NonNull String, Object> producerFactory) {
        LOGGER.info("kafkaTemplate(listeners=%d) method called.".formatted(producerFactory.getListeners().size()));

        return new KafkaTemplate<>(producerFactory);
    }


}
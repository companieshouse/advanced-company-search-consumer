package uk.gov.companieshouse.advancedcompanysearchconsumer.integration;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import uk.gov.companieshouse.advancedcompanysearchconsumer.config.TestKafkaConfig;

import java.time.Duration;

@Testcontainers
@Import(TestKafkaConfig.class)
public abstract class AbstractKafkaIntegrationTest {

    @Container
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(
            "confluentinc/cp-kafka:latest"))
            .withStartupTimeout(Duration.ofMinutes(2))
            .withKraft();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}

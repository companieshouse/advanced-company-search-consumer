package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.UPDATE;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.advancedcompanysearchconsumer.AdvancedCompanySearchConsumerApplication;
import uk.gov.companieshouse.advancedcompanysearchconsumer.config.TestKafkaConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.stream.ResourceChangedData;

/**
 * "Test" class re-purposed to produce {@link ResourceChangedData} messages to the
 * <code>company-stream-profile</code> topic in Tilt. This is NOT to be run as part of an automated
 * test suite. It is for manual testing only.
 */
@SpringBootTest(classes = AdvancedCompanySearchConsumerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations = "classpath:stream-company-profile-in-tilt.properties")
@Import(TestKafkaConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class StreamChangedCompanyProfileInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "StreamCompanyProfileInTiltProducer");

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    @Value("${consumer.topic}")
    private String streamCompanyProfileTopic;

    @Autowired
    private KafkaProducer<String, ResourceChangedData> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
            streamCompanyProfileTopic, 0, System.currentTimeMillis(), "key", UPDATE));
        final var result = future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var partition = result.partition();
        final var offset = result.offset();
        LOGGER.info("Message " + UPDATE + " delivered to topic " + streamCompanyProfileTopic
            + " on partition " + partition + " with offset " + offset + ".");
    }
}
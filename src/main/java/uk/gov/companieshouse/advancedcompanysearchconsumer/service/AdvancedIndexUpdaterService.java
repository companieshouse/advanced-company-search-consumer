package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.logging.LoggingUtils.getLogMap;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that converts the <code>stream-company-profile/code> Kafka message it receives into a
 * REST request it dispatches to update the ElasticSearch advanced company search index.
 */
@Component
public class AdvancedIndexUpdaterService implements Service {

    private final Logger logger;

    public AdvancedIndexUpdaterService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void processMessage(ServiceParameters parameters) {

        final var message = parameters.getData();
        final var resourceId = message.getResourceId();
        final var resourceKind = message.getResourceKind();
        final var resourceUri = message.getResourceUri();

        logger.info("Processing message " + message + " for resource ID " + resourceId +
            ", resource kind " + resourceKind + ", resource URI " + resourceUri + ".",
            getLogMap(message));

    }

}
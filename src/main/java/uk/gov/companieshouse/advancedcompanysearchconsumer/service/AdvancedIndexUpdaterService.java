package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.logging.LoggingUtils.getLogMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that converts the <code>stream-company-profile/code> Kafka message it receives into a
 * REST request it dispatches to update the ElasticSearch advanced company search index.
 */
@Component
public class AdvancedIndexUpdaterService implements Service {

    private final Logger logger;

    private final AdvancedIndexDeleteService advancedIndexDeleteService;

    public AdvancedIndexUpdaterService(Logger logger, AdvancedIndexDeleteService advancedIndexDeleteService) {
        this.logger = logger;
        this.advancedIndexDeleteService = advancedIndexDeleteService;
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

        try {
            var messageType = message.getEvent().getType();
            message.getResourceId();

            switch (messageType) {
                case "changed":
                    logger.debug("This is a 'changed' type message.");
                    break;
                case "deleted":
                    logger.debug("This is a 'deleted' type message.");
                    advancedIndexDeleteService.deleteCompanyFromAdvancedIndex(message.getResourceId());
                    break;
                default:
                    logger.error(String.format("NonRetryable error occurred, unknown message type of %s", messageType));
                    throw new Exception("AdvancedIndexUpdaterService unknown message type.");
            }
        }catch (ApiErrorResponseException apiException) {
            logger.error(String.format("Error response from INTERNAL API: %s", apiException));
            throw new RetryableException("Attempting to retry due to failed API response", apiException);
        } catch (Exception exception) {
            final var rootCause = ExceptionUtils.getRootCause(exception);
            logger.error(String.format("NonRetryable error occurred. Error: %s", rootCause));
            throw new NonRetryableException("AdvancedIndexUpdaterService.processMessage: ", rootCause);
        }
    }
}
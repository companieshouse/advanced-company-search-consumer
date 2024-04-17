package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static com.fasterxml.jackson.databind.util.ClassUtil.getRootCause;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.logging.LoggingUtils.getLogMap;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that converts the <code>stream-company-profile/code> Kafka message it receives into a
 * REST request it dispatches to update the ElasticSearch advanced company search index.
 */
@Component
public class AdvancedIndexUpdaterService implements Service {

    private final CompanyProfileUpsert companyProfileUpsert;

    private final Logger logger;

    public AdvancedIndexUpdaterService(Logger logger, CompanyProfileUpsert companyProfileUpsert) {
        this.logger = logger;
        this.companyProfileUpsert = companyProfileUpsert;
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
            final ApiResponse<Void> upsertAdvancedResponse = companyProfileUpsert.upsertCompanyProfile(parameters);
            logger.info("API returned response: " + upsertAdvancedResponse.getStatusCode());
        } catch (ApiErrorResponseException apiException) {
            logger.error("Error response from INTERNAL API: " + apiException);
            throw new RetryableException("Attempting retry due to failed response", apiException);
        } catch (URIValidationException uriException) {
            logger.error("Error with URI: " + uriException);
            throw new RetryableException("Attempting retry due to URI validation error", uriException);
        } catch (Exception exception) {
            final var rootCause = getRootCause(exception);
            logger.error("NonRetryable Error: " + rootCause);
            throw new NonRetryableException("AdvancedIndexUpdaterService.processMessage: ", rootCause);
        }
    }

}
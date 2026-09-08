package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

@Component
public class AdvancedIndexDeleteService {

    private final Logger logger;
    private final ApiClientService apiClientService;

    public AdvancedIndexDeleteService(Logger logger, ApiClientService apiClientService) {
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    public void deleteCompanyFromAdvancedIndex(String resourceId) throws ApiErrorResponseException, URIValidationException {
        logger.info("deleteCompanyFromAdvancedIndex(companyNumber=%s) method called.".formatted(resourceId));

        String formattedUri = String.format("/advanced-search/companies/%s", resourceId);

        logger.debug("Attempting to delete company profile for company number: %s".formatted(resourceId));
        ApiResponse<Void> apiResponse = apiClientService
                .getInternalApiClient()
                .get()
                .privateSearchResourceHandler()
                .advancedCompanySearch()
                .deleteCompanyProfile(formattedUri)
                .execute();

        logger.debug("API Response: [Status Code: %d, Errors: %d]...".formatted(apiResponse.getStatusCode(),
                apiResponse.getErrors().size()));
    }
}

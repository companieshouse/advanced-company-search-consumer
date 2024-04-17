package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
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
        logger.info("Delete " + resourceId + " from Advanced index!");
        apiClientService
                .getInternalApiClient()
                .privateSearchResourceHandler()
                .advancedCompanySearch()
                .deleteCompanyProfile("/advanced-search/companies/" + resourceId).execute();
    }
}

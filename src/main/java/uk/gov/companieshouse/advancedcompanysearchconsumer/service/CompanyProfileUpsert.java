package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.util.ApiUtils.mapMessageToRequest;
import uk.gov.companieshouse.logging.Logger;

@Component
public class CompanyProfileUpsert {

    private final ApiClientService apiClientService;

    private final Logger logger;

    public CompanyProfileUpsert(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    public ApiResponse<Void> upsertCompanyProfile(ServiceParameters parameters)
            throws ApiErrorResponseException, URIValidationException {

        final String companyName = parameters.getData().get("company_name").toString();
        final String companyNumber = parameters.getData().get("company_number").toString();
        String resourceUri = String.format("/advanced-search/companies/%s", companyNumber);
        final CompanyProfileApi requestBody = mapMessageToRequest(parameters);

        // TODO Log company name and number

        logger.info("Mapping parameters for company profile upsert");

        ApiResponse<Void> response = apiClientService
                .getInternalApiClient()
                .privateSearchResourceHandler()
                .advancedCompanySearch()
                .upsertCompanyProfile(resourceUri, requestBody)
                .execute();

        logger.info("API returned response: "+ response.getStatusCode());

        return response;
    }


}

package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.mapper.CompanyProfileMapper;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
public class AdvancedIndexUpsertService {

    private final Logger logger;
    private final ApiClientService apiClientService;
    private final CompanyProfileMapper mapper;

    public AdvancedIndexUpsertService(Logger logger, ApiClientService apiClientService, CompanyProfileMapper mapper) {
        this.logger = logger;
        this.apiClientService = apiClientService;
        this.mapper = mapper;
    }

    public void upsertCompanyProfileService(ResourceChangedData data) throws ApiErrorResponseException, URIValidationException {
        logger.info("upsertCompanyProfileService(companyNumber=%s) method called.".formatted(data.getResourceId()));

        String companyNumber = data.getResourceId();
        String formattedUri = String.format("/advanced-search/companies/%s", companyNumber);

        CompanyProfileApi companyProfile = mapper.mapToCompanyProfile(data.getData());

        logger.debug("Attempting to upsert company profile for company number: %s".formatted(companyNumber));
        ApiResponse<Void> apiResponse = apiClientService
                .getInternalApiClient()
                .get()
                .privateSearchResourceHandler()
                .advancedCompanySearch()
                .upsertCompanyProfile(formattedUri, companyProfile)
                .execute();

        logger.debug("API Response: [Status Code: %d, Errors: %d]...".formatted(
                apiResponse.getStatusCode(), apiResponse.getErrors().size()));
    }

}

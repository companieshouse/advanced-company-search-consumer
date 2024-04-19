package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.UpsertException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
public class AdvancedIndexUpsertService {

    private final Logger logger;
    private final ApiClientService apiClientService;
    private final CompanyProfileDeserialiser deserialiser;

    public AdvancedIndexUpsertService(Logger logger, ApiClientService apiClientService, CompanyProfileDeserialiser deserialiser) {
        this.logger = logger;
        this.apiClientService = apiClientService;
        this.deserialiser = deserialiser;
    }

    public void upsertCompanyProfileService(ResourceChangedData data) throws ApiErrorResponseException, URIValidationException, UpsertException {

        String companyNumber = data.getResourceId();
        logger.info("Attempting to upsert company: " + companyNumber + "to Advanced Search Index");

        try {
            CompanyProfileApi companyProfile = deserialiser.deserialiseCompanyProfile(data.getData());
            apiClientService
                    .getInternalApiClient()
                    .privateSearchResourceHandler()
                    .advancedCompanySearch()
                    .upsertCompanyProfile("/advanced-search/companies/", companyProfile)
                    .execute();
        } catch (ApiErrorResponseException e) {
            logger.error("Error while upserting message: " + data + "to Advanced Search Index" , e);
        }
    }

}

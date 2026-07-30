package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.AdvancedCompanySearchConsumerApplication.NAMESPACE;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompanyProfileDeserialiser {

    private static final Logger logger = LoggerFactory.getLogger(NAMESPACE);

    private final ObjectMapper objectMapper;

    public CompanyProfileDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CompanyProfileApi deserialiseCompanyProfile(String data) {
        try {
            return objectMapper.readValue(data, CompanyProfileApi.class);
        } catch (JacksonException e) {
            logger.errorContext( "Unable to parse message payload data", e, null);
            throw new NonRetryableException("Unable to parse message payload data", e);
        }
    }

}

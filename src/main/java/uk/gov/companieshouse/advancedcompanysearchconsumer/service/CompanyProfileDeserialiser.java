package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.AdvancedCompanySearchConsumerApplication.NAMESPACE;

@Component
public class CompanyProfileDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final ObjectMapper objectMapper;

    public CompanyProfileDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CompanyProfileApi deserialiseCompanyProfile(String data) {
        try {
            return objectMapper.readValue(data, CompanyProfileApi.class);
        } catch (JsonProcessingException e) {
            LOGGER.errorContext( "Unable to parse message payload data", e, null);
            throw new NonRetryableException("Unable to parse message payload data", e);
        }
    }

}

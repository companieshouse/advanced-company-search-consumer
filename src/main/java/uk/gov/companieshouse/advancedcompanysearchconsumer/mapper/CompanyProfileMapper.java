package uk.gov.companieshouse.advancedcompanysearchconsumer.mapper;

import static uk.gov.companieshouse.advancedcompanysearchconsumer.Application.NAMESPACE;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CompanyProfileMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final JsonMapper mapper;

    public CompanyProfileMapper(final JsonMapper newMapper) {
        this.mapper = newMapper;
    }

    public CompanyProfileApi mapToCompanyProfile(final String data) {
        LOGGER.info("mapToCompanyProfile(data=%s) method called.".formatted(data));
        try {
            return mapper.readValue(data, CompanyProfileApi.class);

        } catch (JacksonException e) {
            LOGGER.errorContext( "Unable to parse message payload data", e, null);
            throw new NonRetryableException("Unable to parse message payload data", e);
        }
    }

}

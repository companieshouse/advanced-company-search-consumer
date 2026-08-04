package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

@ExtendWith(MockitoExtension.class)
class CompanyProfileDeserialiserTest {

    @Mock
    private ObjectMapper mockObjectMapper;

    @InjectMocks
    private CompanyProfileDeserialiser deserialiser;

    @Test
    void whenDeserialisationSuccessful_thenReturnCompanyProfileApi() throws JacksonException {
        final var testData = "Dummy Data";

        when(mockObjectMapper.readValue(testData, CompanyProfileApi.class))
                .thenReturn(mock(CompanyProfileApi.class));

        assertNotNull(deserialiser.deserialiseCompanyProfile("Dummy Data"));
    }

    @Test
    void whenDeserialisationFails_thenThrowsException() throws JacksonException {
        final var testData = "Dummy Data";
        final var mockException = mock(JacksonException.class);

        when(mockObjectMapper.readValue(testData, CompanyProfileApi.class))
                .thenThrow(mockException);

        final var exception = assertThrows(NonRetryableException.class, () ->
            deserialiser.deserialiseCompanyProfile(testData));

        assertThat(exception.getMessage()).isEqualTo("Unable to parse message payload data");
        assertThat(exception.getCause()).isEqualTo(mockException);
    }
}

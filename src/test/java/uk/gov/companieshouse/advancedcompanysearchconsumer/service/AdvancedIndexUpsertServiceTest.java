package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.search.PrivateSearchResourceHandler;
import uk.gov.companieshouse.api.handler.search.advanced.PrivateAdvancedCompanySearchHandler;
import uk.gov.companieshouse.api.handler.search.advanced.request.PrivateAdvancedCompanySearchUpsert;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class AdvancedIndexUpsertServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private CompanyProfileDeserialiser deserialiser;

    @Mock
    private ResourceChangedData data;

    @Mock
    private CompanyProfileApi companyProfile;

    @Mock
    private InternalApiClient apiClient;

    @Mock
    private PrivateSearchResourceHandler resourceHandler;

    @Mock
    private PrivateAdvancedCompanySearchHandler searchHandler;

    @Mock
    private PrivateAdvancedCompanySearchUpsert searchUpsert;

    private AdvancedIndexUpsertService service;

    @BeforeEach
    void setUp() {
        service = new AdvancedIndexUpsertService(logger, apiClientService, deserialiser);

    }

    private void setupMocks() {
        when(data.getResourceId()).thenReturn("12345678");
        when(data.getData()).thenReturn("company profile data");

        when(deserialiser.deserialiseCompanyProfile("company profile data")).thenReturn(companyProfile);
        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privateSearchResourceHandler()).thenReturn(resourceHandler);
        when(resourceHandler.advancedCompanySearch()).thenReturn(searchHandler);

        when(searchHandler.upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile))
                .thenReturn(searchUpsert);
    }

    @Test
    void shouldUpsertCompanyProfile() throws Exception {
        setupMocks();

        service.upsertCompanyProfileService(data);

        verify(deserialiser).deserialiseCompanyProfile("company profile data");
        verify(apiClientService).getInternalApiClient();
        verify(apiClient).privateSearchResourceHandler();
        verify(resourceHandler).advancedCompanySearch();
        verify(searchHandler).upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile);
        verify(searchUpsert).execute();
    }

    @Test
    void shouldBuildCorrectUriFromCompanyNumber() throws Exception {
        setupMocks();

        when(data.getResourceId()).thenReturn("12345678");

        when(searchHandler.upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile))
                .thenReturn(searchUpsert);

        service.upsertCompanyProfileService(data);

        verify(searchHandler).upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile);
    }

    @Test
    void shouldDeserialiseCompanyProfileBeforeUpserting() throws Exception {
        setupMocks();

        service.upsertCompanyProfileService(data);

        verify(deserialiser).deserialiseCompanyProfile("company profile data");
        verify(searchHandler).upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile);
    }

    @Test
    void shouldPropagateApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        setupMocks();

        ApiErrorResponseException exception = mock(ApiErrorResponseException.class);

        when(searchUpsert.execute()).thenThrow(exception);

        assertThrows(ApiErrorResponseException.class,
                () -> service.upsertCompanyProfileService(data)
        );

        verify(searchUpsert).execute();
    }

    @Test
    void shouldPropagateUriValidationException() throws Exception {
        setupMocks();

        URIValidationException exception = new URIValidationException("Invalid URI");

        when(searchHandler.upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile))
                .thenReturn(searchUpsert);

        when(searchUpsert.execute()).thenThrow(exception);

        assertThrows(URIValidationException.class,
                () -> service.upsertCompanyProfileService(data)
        );

        verify(searchHandler).upsertCompanyProfile("/advanced-search/companies/12345678", companyProfile);
    }

    @Test
    void shouldNotCallApiWhenDeserialisationFails() {
        NonRetryableException exception = new NonRetryableException("Unable to deserialise company profile", null);

        when(deserialiser.deserialiseCompanyProfile(anyString())).thenThrow(exception);

        assertThrows(RuntimeException.class,
                () -> service.upsertCompanyProfileService(data)
        );

        verify(deserialiser).deserialiseCompanyProfile(eq(data.getData()));
        verify(searchHandler, never()).upsertCompanyProfile(anyString(), any());
    }
}


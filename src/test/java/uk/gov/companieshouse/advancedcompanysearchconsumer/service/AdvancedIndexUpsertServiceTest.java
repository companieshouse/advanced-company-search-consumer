package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.search.PrivateSearchResourceHandler;
import uk.gov.companieshouse.api.handler.search.advanced.PrivateAdvancedCompanySearchHandler;
import uk.gov.companieshouse.api.handler.search.advanced.request.PrivateAdvancedCompanySearchUpsert;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.UPDATE;

@ExtendWith(MockitoExtension.class)
public class AdvancedIndexUpsertServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateSearchResourceHandler privateSearchResourceHandler;

    @Mock
    private PrivateAdvancedCompanySearchHandler privateAdvancedCompanySearchHandler;

    @Mock
    private PrivateAdvancedCompanySearchUpsert privateAdvancedCompanySearchUpsert;

    @Mock
    private CompanyProfileApi companyProfileApi;

    @Mock
    private CompanyProfileDeserialiser companyProfileDeserialiser;

    @Mock
    private ResourceChangedData resourceChangedData;

    @Mock
    private ServiceParameters parameters;

    @InjectMocks
    private AdvancedIndexUpsertService advancedIndexUpsertService;

    @BeforeEach
    void setUp(){

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateSearchResourceHandler()).thenReturn(privateSearchResourceHandler);
        when(privateSearchResourceHandler.advancedCompanySearch()).thenReturn(privateAdvancedCompanySearchHandler);
        when(privateAdvancedCompanySearchHandler.upsertCompanyProfile("/advanced-search/companies/" + resourceChangedData.getResourceId(), companyProfileApi)).thenReturn(privateAdvancedCompanySearchUpsert);
    }

    @Test
    void processMessageUpsertsCompanyProfile_Success() throws ApiErrorResponseException, URIValidationException {

        // given
        when(resourceChangedData.getData()).thenReturn(UPDATE.getData());
        when(companyProfileDeserialiser.deserialiseCompanyProfile(UPDATE.getData())).thenReturn(companyProfileApi);

        // when
        advancedIndexUpsertService.upsertCompanyProfileService(resourceChangedData);

        verify(privateAdvancedCompanySearchUpsert).execute();

    }

    @Test
    void upsertCompanyProfileIntoAdvancedIndex_Success() throws Exception {
        // when
        when(resourceChangedData.getData()).thenReturn(UPDATE.getData());
        when(companyProfileDeserialiser.deserialiseCompanyProfile(anyString())).thenReturn(companyProfileApi);
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateSearchResourceHandler()).thenReturn(privateSearchResourceHandler);
        when(privateSearchResourceHandler.advancedCompanySearch()).thenReturn(privateAdvancedCompanySearchHandler);
        when(privateAdvancedCompanySearchHandler.upsertCompanyProfile("/advanced-search/companies/" + resourceChangedData.getResourceId(), companyProfileApi)).thenReturn(privateAdvancedCompanySearchUpsert);

        advancedIndexUpsertService.upsertCompanyProfileService(resourceChangedData);

        // then
        verify(companyProfileDeserialiser).deserialiseCompanyProfile(UPDATE.getData());
        verify(apiClientService.getInternalApiClient().privateSearchResourceHandler().advancedCompanySearch(), times(1)).upsertCompanyProfile("/advanced-search/companies/" + resourceChangedData.getResourceId(), companyProfileApi);
    }

}

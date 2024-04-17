package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.search.PrivateSearchResourceHandler;
import uk.gov.companieshouse.api.handler.search.advanced.PrivateAdvancedCompanySearchHandler;
import uk.gov.companieshouse.api.handler.search.advanced.request.PrivateAdvancedCompanySearchDelete;
import uk.gov.companieshouse.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdvancedIndexDeleteServiceTest {

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
    private PrivateAdvancedCompanySearchDelete privateAdvancedCompanySearchDelete;

    @InjectMocks
    private AdvancedIndexDeleteService advancedIndexDeleteService;

    @BeforeEach
    void setUp() {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateSearchResourceHandler()).thenReturn(privateSearchResourceHandler);
        when(privateSearchResourceHandler.advancedCompanySearch()).thenReturn(privateAdvancedCompanySearchHandler);
        when(privateAdvancedCompanySearchHandler.deleteCompanyProfile(anyString())).thenReturn(privateAdvancedCompanySearchDelete);
    }

    @Test
    void deleteCompanyFromAdvancedIndex_SuccessfulDeletion() throws Exception {
        String resourceId = "123456";
        advancedIndexDeleteService.deleteCompanyFromAdvancedIndex(resourceId);

        verify(logger).info("Delete " + resourceId + " from Advanced index!");
        verify(apiClientService.getInternalApiClient().privateSearchResourceHandler().advancedCompanySearch(), times(1)).deleteCompanyProfile("/advanced-search/companies/" + resourceId);
    }

    @Test
    void deleteCompanyFromAdvancedIndex_ExceptionThrown() throws Exception {
        //Simple test to ensure the error is propagated up to the calling service to be handled
        String resourceId = "123456";
        advancedIndexDeleteService.deleteCompanyFromAdvancedIndex(resourceId);

        when(privateAdvancedCompanySearchDelete.execute()).thenThrow(ApiErrorResponseException.class);
        assertThrows(ApiErrorResponseException.class, () -> advancedIndexDeleteService.deleteCompanyFromAdvancedIndex(resourceId));
    }
}


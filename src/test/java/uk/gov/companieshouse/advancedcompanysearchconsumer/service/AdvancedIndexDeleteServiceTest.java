package uk.gov.companieshouse.advancedcompanysearchconsumer.service;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.search.PrivateSearchResourceHandler;
import uk.gov.companieshouse.api.handler.search.advanced.PrivateAdvancedCompanySearchHandler;
import uk.gov.companieshouse.api.handler.search.advanced.request.PrivateAdvancedCompanySearchDelete;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AdvancedIndexDeleteServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private ApiClientService clientService;

    @Mock
    private InternalApiClient apiClient;

    @Mock
    private PrivateSearchResourceHandler resourceHandler;

    @Mock
    private PrivateAdvancedCompanySearchHandler searchHandler
            ;

    @Mock
    private PrivateAdvancedCompanySearchDelete searchDelete;

    private AdvancedIndexDeleteService service;

    @BeforeEach
    void setUp() {
        service = new AdvancedIndexDeleteService(logger, clientService);

        when(clientService.getInternalApiClient()).thenReturn(apiClient);
        when(apiClient.privateSearchResourceHandler()).thenReturn(resourceHandler);
        when(resourceHandler.advancedCompanySearch()).thenReturn(searchHandler);
        when(searchHandler.deleteCompanyProfile("/advanced-search/companies/12345678"))
                .thenReturn(searchDelete);
    }

    @Test
    void shouldDeleteCompanyFromAdvancedIndex() throws Exception {
        service.deleteCompanyFromAdvancedIndex("12345678");

        verify(clientService).getInternalApiClient();
        verify(apiClient).privateSearchResourceHandler();
        verify(resourceHandler).advancedCompanySearch();
        verify(searchHandler).deleteCompanyProfile("/advanced-search/companies/12345678");
        verify(searchDelete).execute();
    }

    @Test
    void shouldBuildCorrectUriFromResourceId() throws Exception {
        service.deleteCompanyFromAdvancedIndex("12345678");

        verify(searchHandler).deleteCompanyProfile("/advanced-search/companies/12345678");
    }

    @Test
    void shouldPropagateApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        when(searchHandler.deleteCompanyProfile("/advanced-search/companies/12345678"))
                .thenReturn(searchDelete);

        ApiErrorResponseException exception = mock(ApiErrorResponseException.class);

        when(searchDelete.execute()).thenThrow(exception);

        assertThrows(ApiErrorResponseException.class,
                () -> service.deleteCompanyFromAdvancedIndex("12345678")
        );

        verify(searchDelete).execute();
    }

    @Test
    void shouldPropagateUriValidationException() throws Exception{
        when(searchHandler.deleteCompanyProfile("/advanced-search/companies/12345678"))
                .thenReturn(searchDelete);

        URIValidationException exception = new URIValidationException("Invalid URI");

        when(searchDelete.execute()).thenThrow(exception);

        assertThrows(URIValidationException.class,
                () -> service.deleteCompanyFromAdvancedIndex("12345678")
        );
    }
}


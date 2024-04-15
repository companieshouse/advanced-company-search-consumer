package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.DELETE_PAYLOAD;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.UPDATE;

@ExtendWith(MockitoExtension.class)
class AdvancedIndexUpdaterServiceTest {

    @Mock
    private AdvancedIndexDeleteService advancedIndexDeleteService;

    @InjectMocks
    private AdvancedIndexUpdaterService advancedIndexUpdaterService;

    @Mock
    private Logger logger;

    @Mock
    private ServiceParameters serviceParameters;

    @Test
    @DisplayName("processMessage() logs message clearly")
    void processMessageLogsMessageClearly() throws IOException {

        // Given
        when(serviceParameters.getData()).thenReturn(UPDATE);

        // When
        advancedIndexUpdaterService.processMessage(serviceParameters);

        // Then
        final var expectedLogMessage = resourceToString("/fixtures/expected-log-message.txt",
            StandardCharsets.UTF_8);
        verify(logger).info(eq(expectedLogMessage), anyMap());

    }


    @Test
    @DisplayName("processMessage() correctly calls the delete service when event.type is 'deleted'")
    public void testProcessMessage_DeletedMessageType() throws ApiErrorResponseException, URIValidationException {

        when(serviceParameters.getData()).thenReturn(DELETE_PAYLOAD);

        advancedIndexUpdaterService.processMessage(serviceParameters);

        verify(advancedIndexDeleteService, times(1)).deleteCompanyFromAdvancedIndex("00006400");
    }

}
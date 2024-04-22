package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.apache.kafka.common.security.oauthbearer.internals.secured.UnretryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.io.IOUtils.resourceToString;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.DELETE_PAYLOAD;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.UPDATE;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.utils.TestConstants.WRONG_EVENT_TYPE_PAYLOAD;

@ExtendWith(MockitoExtension.class)
class AdvancedIndexUpdaterServiceTest {

    @Mock
    private AdvancedIndexDeleteService advancedIndexDeleteService;

    @Mock
    private AdvancedIndexUpsertService advancedIndexUpsertService;

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
    @DisplayName("processMessage() correctly handles 'changed' type message")
    void processMessageHandlesChangedMessageType() {
        // Given
        when(serviceParameters.getData()).thenReturn(UPDATE);

        // When
        advancedIndexUpdaterService.processMessage(serviceParameters);

        // Then
        verify(logger).debug("This is a 'changed' type message.");
        verifyNoInteractions(advancedIndexDeleteService);
    }

    @Test
    @DisplayName("processMessage() correctly calls the delete service when event.type is 'deleted'")
    public void testProcessMessage_DeletedMessageType() throws ApiErrorResponseException, URIValidationException {
        // Given
        when(serviceParameters.getData()).thenReturn(DELETE_PAYLOAD);

        // When
        advancedIndexUpdaterService.processMessage(serviceParameters);

        // Then
        verify(advancedIndexDeleteService, times(1)).deleteCompanyFromAdvancedIndex("00006400");
    }

    @Test
    @DisplayName("processMessage() throws NonRetryableException for unknown message type")
    void processMessageLogsUnknownMessageType() {
        // Given
        when(serviceParameters.getData()).thenReturn(WRONG_EVENT_TYPE_PAYLOAD);

        // When, Then
        assertThrows(NonRetryableException.class, () -> advancedIndexUpdaterService.processMessage(serviceParameters));
    }

    @Test
    @DisplayName("processMessage() correctly handles ApiErrorResponseException such that a RetryableException is thrown")
    void processMessageHandlesRetryableExceptions() throws ApiErrorResponseException, URIValidationException {
        // Given
        when(serviceParameters.getData()).thenReturn(DELETE_PAYLOAD);
        doThrow(ApiErrorResponseException.class).when(advancedIndexDeleteService).deleteCompanyFromAdvancedIndex(anyString());

        // When, Then
        assertThrows(RetryableException.class, () -> advancedIndexUpdaterService.processMessage(serviceParameters));
    }

    @Test
    @DisplayName("processMessage() correctly handles an unknown error from a service such that a NonRetryableException is thrown")
    void processMessageHandlesNonRetryableExceptions() throws ApiErrorResponseException, URIValidationException {
        // Given
        when(serviceParameters.getData()).thenReturn(DELETE_PAYLOAD);
        doThrow(URIValidationException.class).when(advancedIndexDeleteService).deleteCompanyFromAdvancedIndex(anyString());

        // When, Then
        assertThrows(NonRetryableException.class, () -> advancedIndexUpdaterService.processMessage(serviceParameters));
    }
}
package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import uk.gov.companieshouse.advancedcompanysearchconsumer.exception.RetryableException;
import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class AdvancedIndexUpdaterServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private AdvancedIndexDeleteService advancedIndexDeleteService;

    @Mock
    private AdvancedIndexUpsertService advancedIndexUpsertService;

    @Mock
    private ServiceParameters parameters;

    // Replace this with the actual type returned by parameters.getData()
    @Mock
    private ResourceChangedData message;

    @Mock
    private EventRecord event;

    private AdvancedIndexUpdaterService service;

    @BeforeEach
    void setUp() {
        service = new AdvancedIndexUpdaterService(
                logger,
                advancedIndexDeleteService,
                advancedIndexUpsertService
        );

        when(parameters.getData()).thenReturn(message);
        when(message.getResourceId()).thenReturn("12345678");
        when(message.getResourceKind()).thenReturn("company-profile");
        when(message.getResourceUri()).thenReturn("/company/12345678");
        when(message.getEvent()).thenReturn(event);
    }

    @Test
    void shouldUpsertCompanyProfileWhenMessageTypeIsChanged() throws Exception {
        when(event.getType()).thenReturn("changed");

        service.processMessage(parameters);

        verify(advancedIndexUpsertService).upsertCompanyProfileService(message);
        verify(advancedIndexDeleteService, never()).deleteCompanyFromAdvancedIndex(any());
    }

    @Test
    void shouldDeleteCompanyFromAdvancedIndexWhenMessageTypeIsDeleted() throws Exception {
        when(event.getType()).thenReturn("deleted");

        service.processMessage(parameters);

        verify(advancedIndexDeleteService).deleteCompanyFromAdvancedIndex("12345678");
        verify(advancedIndexUpsertService, never()).upsertCompanyProfileService(any());
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenMessageTypeIsUnknown() throws Exception {
        when(event.getType()).thenReturn("unknown");

        NonRetryableException exception = assertThrows(NonRetryableException.class,
                () -> service.processMessage(parameters)
        );

        assertEquals("AdvancedIndexUpdaterService.processMessage: ", exception.getMessage());

        verify(advancedIndexUpsertService, never()).upsertCompanyProfileService(any());
        verify(advancedIndexDeleteService, never()).deleteCompanyFromAdvancedIndex(any());
    }

    @Test
    void shouldThrowRetryableExceptionWhenUpsertThrowsApiErrorResponseException() throws Exception  {
        when(event.getType()).thenReturn("changed");

        ApiErrorResponseException apiException = mock(ApiErrorResponseException.class);

        doThrow(apiException)
                .when(advancedIndexUpsertService)
                .upsertCompanyProfileService(message);

        RetryableException exception = assertThrows(RetryableException.class,
                () -> service.processMessage(parameters)
        );

        assertEquals("Attempting to retry due to failed API response", exception.getMessage());
        assertInstanceOf(ApiErrorResponseException.class, exception.getCause());

        verify(advancedIndexUpsertService).upsertCompanyProfileService(message);
    }

    @Test
    void shouldThrowRetryableExceptionWhenDeleteThrowsApiErrorResponseException() throws Exception {
        when(event.getType()).thenReturn("deleted");

        ApiErrorResponseException apiException = mock(ApiErrorResponseException.class);

        doThrow(apiException)
                .when(advancedIndexDeleteService)
                .deleteCompanyFromAdvancedIndex("12345678");

        RetryableException exception = assertThrows(RetryableException.class,
                () -> service.processMessage(parameters)
        );

        assertEquals("Attempting to retry due to failed API response", exception.getMessage());
        assertInstanceOf(ApiErrorResponseException.class, exception.getCause());

        verify(advancedIndexDeleteService).deleteCompanyFromAdvancedIndex("12345678");
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenUpsertThrowsException() throws Exception{
        when(event.getType()).thenReturn("changed");

        RuntimeException runtimeException = new RuntimeException("Something went wrong");

        doThrow(runtimeException)
                .when(advancedIndexUpsertService)
                .upsertCompanyProfileService(message);

        NonRetryableException exception = assertThrows(NonRetryableException.class,
                () -> service.processMessage(parameters)
        );

        assertEquals("AdvancedIndexUpdaterService.processMessage: ", exception.getMessage());

        verify(advancedIndexUpsertService).upsertCompanyProfileService(message);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenDeleteThrowsException() throws Exception {
        when(event.getType()).thenReturn("deleted");

        RuntimeException runtimeException =
                new RuntimeException("Something went wrong");

        doThrow(runtimeException)
                .when(advancedIndexDeleteService)
                .deleteCompanyFromAdvancedIndex("12345678");

        NonRetryableException exception = assertThrows(NonRetryableException.class,
                () -> service.processMessage(parameters)
        );

        assertEquals("AdvancedIndexUpdaterService.processMessage: ", exception.getMessage());

        verify(advancedIndexDeleteService).deleteCompanyFromAdvancedIndex("12345678");
    }
}

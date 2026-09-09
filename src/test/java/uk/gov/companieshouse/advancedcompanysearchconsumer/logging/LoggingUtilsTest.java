package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

    @Mock
    private ResourceChangedData message;

    @Mock
    private EventRecord event;

    @Test
    void getLogMap_mapsAllFieldsFromResourceChangedData() {
        when(message.getResourceId()).thenReturn("resource-123");
        when(message.getResourceKind()).thenReturn("company-profile");
        when(message.getResourceUri()).thenReturn("/company/00000000");
        when(message.getContextId()).thenReturn("context-456");
        when(message.getEvent()).thenReturn(event);
        when(event.toString()).thenReturn("EventRecord{type=changed}");
        when(message.getData()).thenReturn("{\"some\":\"json\"}");

        Map<String, Object> logMap = LoggingUtils.getLogMap(message);

        assertNotNull(logMap);
        assertEquals("resource-123", logMap.get("resource_id"));
        assertEquals("company-profile", logMap.get("resource_kind"));
        assertEquals("/company/00000000", logMap.get("resource_uri"));
        assertEquals("context-456", logMap.get("context_id"));
        assertEquals("EventRecord{type=changed}", logMap.get("event_record"));
        assertEquals("{\"some\":\"json\"}", logMap.get("data"));
    }

    @Test
    void getLogMap_handlesNullFieldsGracefully() {
        when(message.getResourceId()).thenReturn(null);
        when(message.getResourceKind()).thenReturn(null);
        when(message.getResourceUri()).thenReturn(null);
        when(message.getContextId()).thenReturn(null);
        when(message.getEvent()).thenReturn(event);
        when(event.toString()).thenReturn("EventRecord{}");
        when(message.getData()).thenReturn(null);

        Map<String, Object> logMap = LoggingUtils.getLogMap(message);

        assertNotNull(logMap, "getLogMap should not throw or return null when fields are null");
    }

    @Test
    void getRootCause_returnsDeepestCauseInChain() {
        Throwable root = new IllegalStateException("root cause");
        Throwable middle = new RuntimeException("middle", root);
        Exception top = new Exception("top", middle);

        Throwable result = LoggingUtils.getRootCause(top);

        assertSame(root, result);
    }

    @Test
    void getRootCause_returnsExceptionItselfWhenNoCausePresent() {
        Exception noCause = new Exception("standalone failure");

        Throwable result = LoggingUtils.getRootCause(noCause);

        assertSame(noCause, result);
    }
}
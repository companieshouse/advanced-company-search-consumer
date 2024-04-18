package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.stream.ResourceChangedData;

public class LoggingUtils {

    private LoggingUtils() {}

    public static Map<String, Object> getLogMap(final ResourceChangedData message) {

        final var resourceId = message.getResourceId();
        final var resourceKind = message.getResourceKind();
        final var resourceUri = message.getResourceUri();
        final var contextId = message.getContextId();
        final var event = message.getEvent();
        final var data = message.getData();

        return new DataMap.Builder()
            .resourceId(resourceId)
            .resourceKind(resourceKind)
            .resourceUri(resourceUri)
            .contextId(contextId)
            .eventRecord(event.toString())
            .data(data)
            .build()
            .getLogMap();
    }

    public static  Throwable getRootCause(final Exception exception) {
        final var rootCause = ExceptionUtils.getRootCause(exception);
        return rootCause != null ? rootCause : exception;
    }
}
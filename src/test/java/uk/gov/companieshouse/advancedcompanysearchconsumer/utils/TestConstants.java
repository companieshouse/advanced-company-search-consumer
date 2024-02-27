package uk.gov.companieshouse.advancedcompanysearchconsumer.utils;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

public class TestConstants {

    private TestConstants() {}

    public static final ResourceChangedData UPDATE;

    static {
        try {
            UPDATE = ResourceChangedData.newBuilder()
                .setResourceId("00006400")
                .setResourceKind("company-profile")
                .setResourceUri("/company/00006400")
                .setContextId("22-usZuMZEnZY6W_Kip1539964678")
                .setData(IOUtils.resourceToString("/fixtures/resource-changed-data-data.json",
                    StandardCharsets.UTF_8))
                .setEvent(getEvent())
                .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static EventRecord getEvent() {
        return EventRecord.newBuilder()
            .setPublishedAt("1453896193333")
            .setType("changed")
            .setFieldsChanged(emptyList())
            .build();
    }
}
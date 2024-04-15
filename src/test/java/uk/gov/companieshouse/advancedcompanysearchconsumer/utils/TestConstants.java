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

    public static final ResourceChangedData DELETE_PAYLOAD;

    static {
        try {
            UPDATE = ResourceChangedData.newBuilder()
                .setResourceId("00006400")
                .setResourceKind("company-profile")
                .setResourceUri("/company/00006400")
                .setContextId("22-usZuMZEnZY6W_Kip1539964678")
                .setData(IOUtils.resourceToString("/fixtures/resource-changed-data-data.json",
                    StandardCharsets.UTF_8))
                .setEvent(getEvent("changed"))
                .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
            DELETE_PAYLOAD = ResourceChangedData.newBuilder()
                    .setResourceId("00006400")
                    .setResourceKind("company-profile")
                    .setResourceUri("/company/00006400")
                    .setContextId("22-usZuMZEnZY6W_Kip1539964678")
                    .setData("")
                    .setEvent(getEvent("deleted"))
                    .build();
    }

    private static EventRecord getEvent(String type) {
        return EventRecord.newBuilder()
            .setPublishedAt("1453896193333")
            .setType(type)
            .setFieldsChanged(emptyList())
            .build();
    }
}
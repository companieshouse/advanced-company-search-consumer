package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.util.DataMap;

class DataMapHolderTest {

    @AfterEach
    void tearDown() {
        // ThreadLocal state leaks across tests on the same thread
        // (e.g. the JUnit test-runner thread) unless explicitly cleared.
        DataMapHolder.clear();
    }

    @Test
    void getRequestId_defaultsToUninitialisedBeforeInitialiseIsCalled() {
        assertEquals("uninitialised", DataMapHolder.getRequestId());
    }

    @Test
    void initialise_setsRequestIdOnCurrentThread() {
        DataMapHolder.initialise("req-123");

        assertEquals("req-123", DataMapHolder.getRequestId());
    }

    @Test
    void get_returnsBuilderReflectingPriorInitialise() {
        DataMapHolder.initialise("req-456");

        DataMap.Builder builder = DataMapHolder.get();

        assertNotNull(builder);
        assertEquals("req-456", builder.build().getLogMap().get("request_id"));
    }

    @Test
    void getLogMap_containsRequestIdEntry() {
        DataMapHolder.initialise("req-789");

        Map<String, Object> logMap = DataMapHolder.getLogMap();

        assertEquals("req-789", logMap.get("request_id"));
    }

    @Test
    void clear_resetsRequestIdBackToDefaultOnCurrentThread() {
        DataMapHolder.initialise("req-to-be-cleared");

        DataMapHolder.clear();

        assertEquals("uninitialised", DataMapHolder.getRequestId());
    }

    @Test
    void initialise_isIsolatedPerThread() throws InterruptedException {
        DataMapHolder.initialise("main-thread-request");

        AtomicReference<String> otherThreadRequestId = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread other = new Thread(() -> {
            try {
                // Should see the ThreadLocal default, NOT "main-thread-request",
                // since ThreadLocal state must not leak across threads.
                otherThreadRequestId.set(DataMapHolder.getRequestId());
                DataMapHolder.initialise("other-thread-request");
                otherThreadRequestId.set(DataMapHolder.getRequestId());
            } finally {
                DataMapHolder.clear();
                latch.countDown();
            }
        });
        other.start();
        latch.await();

        assertEquals("other-thread-request", otherThreadRequestId.get());
        // Main thread's value must be unaffected by the other thread's initialise().
        assertEquals("main-thread-request", DataMapHolder.getRequestId());
    }
}
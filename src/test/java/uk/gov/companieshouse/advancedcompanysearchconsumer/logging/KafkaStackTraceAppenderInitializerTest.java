package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;

/**
 * Note: KafkaStackTraceAppenderInitializer has no injected dependencies -
 * it reaches out to the static SLF4J/Logback API directly. So instead of
 * mocking collaborators, we intercept the static LoggerFactory call and
 * hand back a Mockito spy around the REAL LoggerContext. That way Logback's
 * internal start-up logic (encoder/appender wiring) still runs for real -
 * avoiding brittle NPEs from a fully mocked context - while still letting
 * us verify() the interaction and inspect the resulting appender.
 */
class KafkaStackTraceAppenderInitializerTest {

    private static final String LOGGER_NAME = "org.springframework.kafka";
    private static final String APPENDER_NAME = "KAFKA_PLAIN";

    private final LoggerContext realContext =
            (LoggerContext) LoggerFactory.getILoggerFactory();

    @AfterEach
    void cleanUp() {
        // Detach whatever we added so this test doesn't leak state into
        // other tests that share the same static Logback context.
        realContext.getLogger(LOGGER_NAME).detachAppender(APPENDER_NAME);
    }

    @Test
    void afterPropertiesSet_attachesStartedConsoleAppenderToKafkaLogger() {
        Logger realLogger = realContext.getLogger(LOGGER_NAME);
        Logger loggerSpy = spy(realLogger);
        LoggerContext contextSpy = spy(realContext);
        doReturn(loggerSpy).when(contextSpy).getLogger(LOGGER_NAME);

        try (MockedStatic<LoggerFactory> mockedFactory = mockStatic(LoggerFactory.class)) {
            mockedFactory.when(LoggerFactory::getILoggerFactory).thenReturn(contextSpy);

            new KafkaStackTraceAppenderInitializer().afterPropertiesSet();

            // Declaring the captor's type explicitly (rather than letting it be
            // inferred from the raw ConsoleAppender.class literal) avoids a
            // wildcard-capture mismatch against addAppender(Appender<ILoggingEvent>).
            @SuppressWarnings("unchecked")
            ArgumentCaptor<ConsoleAppender<ILoggingEvent>> captor = ArgumentCaptor.forClass(ConsoleAppender.class);
            verify(loggerSpy, times(1)).addAppender(captor.capture());

            ConsoleAppender<ILoggingEvent> appender = captor.getValue();
            assertEquals(APPENDER_NAME, appender.getName(), "appender should be named KAFKA_PLAIN");
            assertTrue(appender.isStarted(), "appender should be started");
            assertNotNull(appender.getEncoder(), "appender should have an encoder configured");
            assertTrue(appender.getEncoder().isStarted(), "encoder should be started");
        }
    }

    @Test
    void afterPropertiesSet_isIdempotentAndDoesNotThrow_whenCalledTwice() {
        // Calling it twice attaches two appenders with the same name - this
        // documents current behavior; if that's not desired, the class
        // should guard against duplicate registration.
        assertDoesNotThrow(() -> {
            new KafkaStackTraceAppenderInitializer().afterPropertiesSet();
            new KafkaStackTraceAppenderInitializer().afterPropertiesSet();
        });
    }
}
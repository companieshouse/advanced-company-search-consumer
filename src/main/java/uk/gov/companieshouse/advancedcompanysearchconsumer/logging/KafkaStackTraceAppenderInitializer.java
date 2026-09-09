package uk.gov.companieshouse.advancedcompanysearchconsumer.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
class KafkaStackTraceAppenderInitializer implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{ISO8601} %-5level %logger{60} - %msg%n%ex{full}");
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("KAFKA_PLAIN");
        appender.setEncoder(encoder);
        appender.start();

        context.getLogger("org.springframework.kafka").addAppender(appender);
    }
}
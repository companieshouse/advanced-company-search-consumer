package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;

@Component
public class NonRetryableExceptionService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableErrorException("Unable to handle message",
            new Exception("Unable to handle message"));
    }
}
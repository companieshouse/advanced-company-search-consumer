package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import uk.gov.companieshouse.advancedcompanysearchconsumer.util.ServiceParameters;
/**
 * Processes an incoming message.
 */
public interface Service {

    /**
     * Processes an incoming message.
     *
     * @param parameters Any parameters required when processing the message.
     */
    void processMessage(ServiceParameters parameters);
}
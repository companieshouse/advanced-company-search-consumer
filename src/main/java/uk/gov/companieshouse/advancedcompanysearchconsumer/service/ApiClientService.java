package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientService {

    private final String apiUrl;

    public ApiClientService(@Value("${chs.api.url}") String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public InternalApiClient getInternalApiClient() {
        final var client = ApiSdkManager.getPrivateSDK();
        client.setInternalBasePath(apiUrl);
        return client;
    }

}

package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientService {

    public Supplier<InternalApiClient> getInternalApiClient() {
        return ApiSdkManager::getPrivateSDK;
    }

}
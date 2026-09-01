package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceTest {

    ApiClientService apiClientService;

    @BeforeEach
    void setUp() {
        apiClientService = new ApiClientService();
    }

    @Test
    void testInternalApiClient() {
        assertThrows(EnvironmentVariableException.class,
                () -> apiClientService.getInternalApiClient()
        );
    }
}

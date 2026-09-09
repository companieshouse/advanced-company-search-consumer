package uk.gov.companieshouse.advancedcompanysearchconsumer.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.advancedcompanysearchconsumer.config.TestKafkaConfig;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test_main_positive")
@Import(TestKafkaConfig.class)
class HealthCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Successfully returns health status")
    void returnHealthStatusSuccessfully() throws Exception {
        mockMvc.perform(get("/healthcheck")).andExpect(status().isOk());
    }

}

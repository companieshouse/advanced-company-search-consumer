package uk.gov.companieshouse.advancedcompanysearchconsumer;

import static org.springframework.boot.SpringApplication.run;
import static uk.gov.companieshouse.advancedcompanysearchconsumer.environment.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static final String NAMESPACE = "advanced-company-search-consumer";

    public static void main(String[] args) {
        if (allRequiredEnvironmentVariablesPresent()) {
            run(Application.class, args);
        }
    }

}

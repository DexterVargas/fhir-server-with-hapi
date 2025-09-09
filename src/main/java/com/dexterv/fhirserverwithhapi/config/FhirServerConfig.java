package com.dexterv.fhirserverwithhapi.config;

import ca.uhn.fhir.context.FhirVersionEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;

@Configuration
@Import(FhirTesterMvcConfig.class)
public class FhirServerConfig {

    @Bean
    public TesterConfig testerConfig() {
        TesterConfig config = new TesterConfig();
        config
            .addServer()
            .withId("home")
            .withFhirVersion(FhirVersionEnum.R5)
            .withBaseUrl("http://localhost:8484/fhir/")
            .withName("Local FHIR Server");

        return config;
    }

}

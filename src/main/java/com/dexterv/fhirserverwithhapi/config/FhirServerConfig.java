package com.dexterv.fhirserverwithhapi.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import com.dexterv.fhirserverwithhapi.provider.PatientResourceProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;


@Configuration
@Import(FhirTesterMvcConfig.class)
public class FhirServerConfig {

//    @Bean
//    public RestfulServer restfulServer(PatientResourceProvider patientProvider) {
//
//        RestfulServer server = new RestfulServer(FhirContext.forR5());
//
//        // Register your provider
//        server.setResourceProviders(patientProvider);
//
//
//
//        // Optional defaults
//        server.setDefaultPrettyPrint(true);
//        server.setDefaultResponseEncoding(ca.uhn.fhir.rest.api.EncodingEnum.JSON);
//
//        return server;
//    }

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServlet(PatientResourceProvider patientProvider) {
        RestfulServer server = new RestfulServer(FhirContext.forR5());
        server.setResourceProviders(patientProvider);
        server.setDefaultPrettyPrint(true);
        server.setDefaultResponseEncoding(ca.uhn.fhir.rest.api.EncodingEnum.JSON);
        server.registerInterceptor(new ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor());


        return new ServletRegistrationBean<>(server, "/fhir/*"); // map servlet to /fhir
    }

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

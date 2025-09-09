package com.dexterv.fhirserverwithhapi.servlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import com.dexterv.fhirserverwithhapi.provider.PatientResourceProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FhirRestfulServer {

    @Bean
    public ServletRegistrationBean<RestfulServer> fhirServlet(PatientResourceProvider patientProvider) {
        RestfulServer server = new RestfulServer(FhirContext.forR5());

//        Use this when add new RESOURCES
//        List<IResourceProvider> providers = new ArrayList<>();
//        providers.add(new PatientResourceProvider());
//        setResourceProviders(providers);

        server.setResourceProviders(patientProvider);
        server.setDefaultPrettyPrint(true);
        server.setDefaultResponseEncoding(EncodingEnum.JSON);
        server.registerInterceptor(new ResponseHighlighterInterceptor());
        server.setServerName("My FhirServerWithHapi");
        server.setServerVersion("1.0.0");

        return new ServletRegistrationBean<>(server, "/fhir/*"); // map servlet to /fhir
    }

}

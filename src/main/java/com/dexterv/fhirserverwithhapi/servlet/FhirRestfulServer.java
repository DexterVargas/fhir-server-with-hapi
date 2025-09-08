package com.dexterv.fhirserverwithhapi.servlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import com.dexterv.fhirserverwithhapi.provider.PatientResourceProvider;
import jakarta.servlet.annotation.WebServlet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class FhirRestfulServer extends RestfulServer {

    public FhirRestfulServer() {
        super(FhirContext.forR5());
    }

    @Override
    protected void initialize(){
        // Tell HAPI FHIR that we are using R5
//        setFhirContext(FhirContext.forR5());

        // Register resource providers
//        registerProviders(Collections.singletonList(new PatientResourceProvider()));

        List<IResourceProvider> providers = new ArrayList<>();
        providers.add(new PatientResourceProvider());
        setResourceProviders(providers);


        // Optional: set server name/version
        setServerName("My FhirServerWithHapi");
        setServerVersion("1.0.0");

        /*
         * Use nice coloured HTML when a browser is used to request the content
         */
        registerInterceptor(new ResponseHighlighterInterceptor());
    }

}

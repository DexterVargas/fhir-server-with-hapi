package com.dexterv.fhirserverwithhapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication

public class FhirServerWithHapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhirServerWithHapiApplication.class, args);
    }



}

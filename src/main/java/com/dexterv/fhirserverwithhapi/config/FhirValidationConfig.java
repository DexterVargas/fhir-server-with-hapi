package com.dexterv.fhirserverwithhapi.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirValidationConfig {

//    private final FhirContext fhirContext = FhirContext.forR5();
    // Also expose FhirContext as a bean
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR5();
    }
    @Bean
    public FhirValidator fhirValidator(FhirContext fhirContext) {
        FhirValidator fhirValidator = fhirContext.newValidator();

        FhirInstanceValidator fhirInstanceValidator = new FhirInstanceValidator(fhirContext);
        fhirInstanceValidator.setBestPracticeWarningLevel(BestPracticeWarningLevel.Warning);

        fhirValidator.registerValidatorModule(fhirInstanceValidator);
        return fhirValidator;
    }


}

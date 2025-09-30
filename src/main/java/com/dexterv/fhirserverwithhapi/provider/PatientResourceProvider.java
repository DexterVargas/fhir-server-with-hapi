package com.dexterv.fhirserverwithhapi.provider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import com.dexterv.fhirserverwithhapi.repositories.PatientRepository;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PatientResourceProvider implements IResourceProvider {

    private final PatientRepository patientRepository;
    private final FhirContext fhirContext;
    private final FhirValidator fhirValidator;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    // https://hapifhir.io/hapi-fhir/docs/server_plain/resource_providers.html#resource-providers
    // https://hapifhir.io/hapi-fhir/apidocs/hapi-fhir-server/index-all.html
    // https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations.html

    /**
     * The "@Create" annotation indicates that this method implements "create=type", which adds a
     * new instance of a resource to the server.
     */

    // @Create()  What it is: A HAPI-FHIR server annotation that marks this method as the handler for create operations (i.e. HTTP POST /Patient).
            //    What HAPI does: When a client POSTs a Patient resource, HAPI will call this method and pass the parsed Patient object.
    //    MethodOutcome — HAPI-FHIR class used to describe the result of the operation (id, created flag, OperationOutcome, returned resource, etc.).
    //                    HAPI uses it to generate the HTTP response.
    //    @ResourceParam Patient patient — HAPI injects the incoming resource (parsed from the HTTP body) into this parameter.
    //    You get a Patient object already populated from the request.
    @Create()
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        LocalDateTime localDateTime = LocalDateTime.now();
        // convert LocalDateTime → Date
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Long resourceId = patientRepository.getNextResourceId();

        // Manual add identifier atm
        Identifier identifier = patient.addIdentifier();
        identifier.setSystem("http://hospital.example.org/mrn"); // I just set it to this atm
        String randomVal = "MRN" + UUID.randomUUID().toString();
        identifier.setValue(randomVal);
        // **** hapi fhir fluent coding
        // patient.addIdentifier().setSystem("com.dexterv.fhirserverwithhapi").setValue(randomVal);

        validateWithFhirSpec(patient);
        validateResourceWithCustomRules(patient);

        String json = FhirContext.forR5().newJsonParser().encodeResourceToString(patient);
        PatientEntity entity = PatientEntity.builder()
                .resourceId(resourceId)
                .version(1)
                .resource(json)
                .lastUpdated(localDateTime)
                .build();

        patientRepository.save(entity);

        // Set Patient resource Patient/<logical id> and set default version ID for new Patient resource
        patient.setId(
                new IdType(
                        "Patient",
                        entity.getResourceId()));
        patient.getMeta().setVersionId("1");
        patient.getMeta().setLastUpdated(date);
        // if you want to

        MethodOutcome outcome = new MethodOutcome();
        outcome.setCreated(true);
        // outcome.setId(patient.getIdElement());
        outcome.setId(new IdType("Patient", entity.getResourceId().toString(), "1"));
        outcome.setResource(patient);

        return outcome;

    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
     * </p>
     *
     * @param theId The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
    @Read(version = true)
    public Patient readPatientById(@IdParam IdType theId) {

        long resourceId;

        try {
            resourceId = Long.parseLong(theId.getIdPart());
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid with ID " + theId.getIdPart() + " not found");
        }
        PatientEntity entity;
        if (theId.hasVersionIdPart()) {
            Integer versionId = Integer.parseInt(theId.getVersionIdPart());

            entity = patientRepository.findByResourceIdAndVersion(resourceId, versionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient/" + resourceId + "/_history/" + versionId + " not found"));
        } else {
            entity = patientRepository.findTopByResourceIdOrderByVersionDesc(resourceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + resourceId + " not found"));
        }

        Patient patient = (Patient) fhirContext
                .newJsonParser()
                .parseResource(entity.getResource());

        // 🔑 Always set the FHIR id before returning
        patient.setId(new IdType("Patient", entity.getResourceId().toString(), entity.getVersion().toString()));

        return patient;
    }

    /**
     * The "@Update" annotation indicates that this method supports replacing an existing
     * resource (by ID) with a new instance of that resource.
     *
     * @param theId      This is the ID of the patient to update
     * @param incomingPatient This is the actual resource to save
     * @return This method returns a "MethodOutcome"
     */
    @Update()
    public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient incomingPatient) {
        LocalDateTime localDateTime = LocalDateTime.now();
        // convert LocalDateTime → Date
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Long resourceId = getValidId(theId, incomingPatient);
//        String versionId = theId.getVersionIdPart();

        validateWithFhirSpec(incomingPatient);
        validateResourceWithCustomRules(incomingPatient);

//        String json = FhirContext.forR5().newJsonParser().encodeResourceToString(patient);

        // 1. Get latest version of the patient
        PatientEntity latest = patientRepository.findTopByResourceIdOrderByVersionDesc(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + resourceId + " not found"));

//      // 2. Parse existing JSON into FHIR Patient
        Patient existingPatient = (Patient) fhirContext
                .newJsonParser()
                .parseResource(latest.getResource());
        
        // 3. Merge Updates
        mergePatient(existingPatient, incomingPatient);

        String newJSON = FhirContext.forR5().newJsonParser().encodeResourceToString(existingPatient);

        // 4. Create new DB row for versioned resources
        PatientEntity newVersion = PatientEntity.builder()
                .resourceId(resourceId)
                .version(latest.getVersion() + 1)
                .resource(newJSON)
                .lastUpdated(localDateTime)
                .build();

        patientRepository.save(newVersion);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", resourceId.toString(), newVersion.getVersion().toString()));
        outcome.setResource(existingPatient);

        return outcome;

    }

    private void mergePatient(Patient existing, Patient incoming) {
        if (incoming.hasName()) {
            existing.setName(incoming.getName());
        }
        if (incoming.hasGender()) {
            existing.setGender(incoming.getGender());
        }
        if (incoming.hasBirthDate()) {
            existing.setBirthDate(incoming.getBirthDate());
        }
        // 🔑 Leave other fields untouched unless explicitly updated
    }

    private static @NotNull Long getValidId(IdType theId, Patient patient) {
        if (theId == null || !theId.hasIdPart()) {
            throw new InvalidRequestException("Missing resource ID in URL");
        }

        if (!patient.hasId()) {
            throw new InvalidRequestException("Missing resource ID in body");
        }

        String urlId = theId.getIdPart();
        String bodyId = patient.getIdElement().getIdPart();

        if (!urlId.equals(bodyId)) {
            throw new InvalidRequestException(
                    String.format("Mismatched IDs: URL has %s but body has %s", urlId, bodyId)
            );
        }

        try {
            return Long.parseLong(urlId);
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Patient ID must be numeric, got: " + urlId);
        }
    }

    @History
    public List<IBaseResource> historyPatient(@IdParam IdType theId) {
        long resourceId;

        try {
            resourceId = Long.parseLong(theId.getIdPart());
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid with ID " + theId.getIdPart() + " not found");
        }

        List<PatientEntity> patientEntities = patientRepository.findAllByResourceIdOrderByVersionDesc(resourceId);

        return patientEntities.stream()
                .map(e-> {
                    Patient patient = (Patient) fhirContext
                            .newJsonParser()
                            .parseResource(e.getResource());
                    // 🔑 Always set the FHIR id before returning
                    patient.setId(new IdType("Patient", e.getResourceId().toString(), e.getVersion().toString()));
                    return patient;
                })
                .collect(Collectors.toList());
    }

    @Search
    public Bundle searchPatient(
            @OptionalParam(name = Patient.SP_FAMILY) StringParam family,
            @OptionalParam(name = Patient.SP_GENDER) StringParam gender) {

        List<PatientEntity> entities = patientRepository.findAll(); // fetch all

        // I don't have separate columns (like family, gender)
        // need to store searchable FHIR attributes (like family, given, gender) in separate DB columns in PatientEntity.
        // Will map all search result at the moment
        List<Patient> matched = entities.stream()
                .map(e -> {
                    Patient patient = (Patient) fhirContext.newJsonParser().parseResource(Patient.class, e.getResource());
                    patient.setId(new IdType("Patient", e.getResourceId().toString(), e.getVersion().toString()));
                    return patient;
                })
                .filter(p -> {
                    boolean matches = true;
                    if (family != null) {
                        matches &= p.getName().stream()
                                .anyMatch(n -> family.getValue().equalsIgnoreCase(n.getFamily()));
                    }
                    if (gender != null) {
                        matches &= gender.getValue().equalsIgnoreCase(p.getGender().toCode());
                    }
                    return matches;
                })
                .toList();

        // Build a Bundle response (FHIR standard for searches)
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(matched.size());

        matched.forEach(p -> bundle.addEntry()
                .setResource(p)
                .setFullUrl("Patient/" + p.getIdElement().getIdPart()));

        return bundle;
    }

    @Search
    public List<Patient> getAllPatients(
//            @OptionalParam(name = "_count") Integer count,
//            @OptionalParam(name = "_offset") Integer offset
    ) {
//        Integer pageSize = (count != null && count>0) ? count : 10;
//        Integer pageOffset = (offset != null && offset>0) ? offset : 0;
//        Pageable pageable = PageRequest.of(pageOffset / pageSize, pageSize);
//        List<PatientEntity> entities = patientRepository.findAll(pageable).getContent();

        List<PatientEntity> entities = patientRepository.findAll();

        List<Patient> patients = new ArrayList<>();

        for (PatientEntity entity : entities) {
            System.out.println(entity);
            Patient patient = (Patient) fhirContext
                    .newJsonParser()
                    .parseResource(entity.getResource());
            patient.setId("Patient/" + entity.getId());
            patients.add(patient);
        }

        return patients;
    }

    /**
     * This method just provides simple business validation for resources we are storing.
     *
     * @param patient The patient to validate
     */
    private void validateResourceWithCustomRules(Patient patient) {

        if (!patient.hasName() || patient.getNameFirstRep().getFamily() == null) {
            throw new InvalidRequestException("Patient must have a family name");
        }

        if (!patient.hasGender()) {
            throw new InvalidRequestException("Patient must have a gender");
        }

        if (patient.hasBirthDate() && patient.getBirthDate().after(new Date())) {
            throw new InvalidRequestException("Patient birthDate cannot be in the future");
        }
    }

    /**
     * This method comply with FHIR specs validation
     *
     * @param patient The patient to validate
     */
    private void validateWithFhirSpec(Patient patient) {

        ValidationResult result = fhirValidator.validateWithResult(patient);

        if (!result.isSuccessful()) {
            OperationOutcome outcome = new OperationOutcome();

            for (SingleValidationMessage msg : result.getMessages()) {
                OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
                issue.setSeverity(OperationOutcome.IssueSeverity.fromCode(msg.getSeverity().getCode()));
                issue.setCode(OperationOutcome.IssueType.PROCESSING);
                issue.setDiagnostics(msg.getMessage());
            }

            throw new UnprocessableEntityException(fhirContext, outcome);
        }

    }
}

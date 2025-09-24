package com.dexterv.fhirserverwithhapi.provider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import com.dexterv.fhirserverwithhapi.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PatientResourceProvider implements IResourceProvider {

    private final PatientRepository patientRepository;
    private final FhirContext fhirContext = FhirContext.forR5();

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    // https://hapifhir.io/hapi-fhir/docs/server_plain/resource_providers.html#resource-providers
    // https://hapifhir.io/hapi-fhir/apidocs/hapi-fhir-server/index-all.html
    // https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations.html

    /**
     * Stores a new version of the patient in memory so that it can be retrieved later.
     *
     * @param patient The patient resource to store
     * @param Id      The ID of the patient to retrieve
     */
//    public void addNewVersion(Patient patient,  UUID Id) {
//        if(!myIdToPatientVersions.containsKey(Id)) {
//            myIdToPatientVersions.put(Id, new LinkedList<>());
//        }
//
//        patient.getMeta().setLastUpdatedElement(InstantType.withCurrentTime());
//
//        Deque<Patient> existingVersions = myIdToPatientVersions.get(Id);
//
//        // We just use the current number of versions as the next version number
//        String newVersion = Integer.toString(existingVersions.size());
//
//        // Create an ID with the new version and assign it back to the resource
//        IdType newId = new IdType("Patient", Long.toString(Id), newVersion);
//        patient.setId(newId);
//
//        existingVersions.add(patient);
//    }
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

        // Manual add identifier atm
        Identifier identifier = patient.addIdentifier();
        identifier.setSystem("com.dexterv.fhirserverwithhapi"); // I just set it to this atm
        String randomVal = "dexterv" + UUID.randomUUID().toString();
        identifier.setValue(randomVal);

        // **** hapi fhir fluent coding
        // patient.addIdentifier().setSystem("com.dexterv.fhirserverwithhapi").setValue(randomVal);

        validateResource(patient);

        String json = FhirContext.forR5().newJsonParser().encodeResourceToString(patient);

        PatientEntity patientEntity = new PatientEntity();
        // PatientEntity patientEntity = PatientEntity.builder().resource(json).build();
        patientEntity.setResource(json);
        patientEntity.setVersion(1);


        LocalDateTime localDateTime = LocalDateTime.now();

// convert LocalDateTime → Date
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());


        patientEntity.setLastUpdated(localDateTime);
        PatientEntity savedEntity = patientRepository.save(patientEntity);

        patientEntity.setResourceId(savedEntity.getId());
        // Set Patient resource Patient/<logical id> and set default version ID for new Patient resource
        patient.setId(
                new IdType(
                        "Patient",
                        patientEntity.getResourceId()));
        patient.getMeta().setVersionId("1");
        patient.getMeta().setLastUpdated(date);
        // if you want to


        MethodOutcome outcome = new MethodOutcome();
        outcome.setCreated(true);
//        outcome.setId(patient.getIdElement());
        outcome.setId(new IdType("Patient", patientEntity.getResourceId().toString(), "1"));
        outcome.setResource(patient);

        return outcome;

    }

    /**
     * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
     * <p>
     * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
     * </p>
     *
     * @param id The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
     * @return Returns a resource matching this identifier, or null if none exists.
     */
//    @Read(version = true)
//    public Patient readPatientById(@IdParam IdType id) {
//
//        long patientId;
//
//        try {
//            patientId = Long.parseLong(id.getIdPart());
//        } catch (NumberFormatException e) {
//            throw new ResourceNotFoundException("Invalid with ID " + id.getIdPart() + " not found");
//        }
//
//
//        System.out.println("Reading Patient by ID: " + patientId);
//
//        PatientEntity entity = patientRepository.findById(patientId)
//                .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + patientId + " not found"));
//
//        System.out.println(entity.getId() + ": " + entity.getResource());
//        Patient patient = (Patient) fhirContext
//                .newJsonParser()
//                .parseResource(entity.getResource());
//
//        // 🔑 Always set the FHIR id before returning
//        patient.setId("Patient/" + entity.getId());
//
//        return patient;
////        return (Patient) fhirContext.newJsonParser()
////                .parseResource(patientEntity.getResource());
//    }

    /**
     * The "@Update" annotation indicates that this method supports replacing an existing
     * resource (by ID) with a new instance of that resource.
     *
     * @param id      This is the ID of the patient to update
     * @param patient This is the actual resource to save
     * @return This method returns a "MethodOutcome"
     */
//    @Update()
//    public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient patient) {
//        Long resourceId = getValidId(theId, patient);
////        String versionId = theId.getVersionIdPart();
//
//        validateResource(patient);
//
//        String json = FhirContext.forR5().newJsonParser().encodeResourceToString(patient);
//
//        PatientEntity patientEntity = patientRepository.findById(resourceId)
//                .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + resourceId + " not found"));
//
////        if(versionId != null) {
////            if(!versionId.equals(patientEntity.getVersion())) {
////
////            }
////        }
//
//        patientEntity.setResource(json);
//
//        PatientEntity savedEntity = patientRepository.save(patientEntity);
//
//        System.out.println("patient getId " + savedEntity.getId());
//
//        // Set Patient resource Patient/<logical id> and set default version ID for new Patient resource
//        patient.setId(
//                new IdType(
//                        "Patient",
//                        savedEntity.getId().toString()));
//        patient.getMeta().setVersionId(savedEntity.getVersion().toString());
//        // if you want to
//
//
//        MethodOutcome outcome = new MethodOutcome();
//        System.out.println("outcome area: " + savedEntity.getResource());
//        outcome.setCreated(true);
////        outcome.setId(patient.getIdElement());
//        outcome.setId(new IdType("Patient", savedEntity.getId().toString(), savedEntity.getVersion().toString()));
//        outcome.setResource(patient);
//
//        System.out.println("outcome: " + outcome.getId());
//        return outcome;
//
//        /*
//         * Throw an exception (HTTP 404) if the ID is not known
//         */
////        if(!myIdToPatientVersions.containsKey(idHandler)){
////            throw new ResourceNotFoundException(id);
////        }
//
//
//
////        return new MethodOutcome();
//    }

    private static @NotNull Long getValidId(IdType theId, Patient patient) {
        Long resourceId;

        try {
            resourceId = Long.parseLong(theId.getIdPart());
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid ID " + theId.getValue() + " - Must be numeric");
        }

        if(!theId.getIdPart().equals(patient.getId())) {
            throw new InvalidRequestException(String.format("Cannot update Patient with id %s not found", resourceId));
        }

        return resourceId;
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
            // 🔑 Set ID (must exist in FHIR)
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
    private void validateResource(Patient patient) {
        if (patient.getNameFirstRep().getFamily() == null || patient.getNameFirstRep().getFamily().isEmpty()) {
            OperationOutcome outcome = new OperationOutcome();
            outcome.addIssue()
                    .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                    .setDiagnostics("No Family name provided, Patient resources must have at least one family name.");
            throw new UnprocessableEntityException(fhirContext,  outcome);
        }

        if(patient.getIdentifierFirstRep().isEmpty()) {
            throw new UnprocessableEntityException(Msg.code(636) + "No identifier supplied");
        }
    }
}

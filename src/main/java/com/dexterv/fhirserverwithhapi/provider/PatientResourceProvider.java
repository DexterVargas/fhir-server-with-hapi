package com.dexterv.fhirserverwithhapi.provider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import com.dexterv.fhirserverwithhapi.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.zip.DataFormatException;

@Component
@RequiredArgsConstructor
public class PatientResourceProvider implements IResourceProvider {

    private final PatientRepository patientRepository;
    private final FhirContext fhirContext = FhirContext.forR5();

    // you must return the type of resource this provider serves:
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }


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

    @Create()
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        validateResource(patient);

//        SAVE to JPA Postgress
        String json = FhirContext.forR5().newJsonParser().encodeResourceToString(patient);
        PatientEntity patientEntity = new PatientEntity();
//        PatientEntity patientEntity = PatientEntity.builder().resource(json).build();

        patientEntity.setResource(json);
        patientRepository.save(patientEntity);

        // Set FHIR ID from DB ID
        patient.setId("Patient/" + patientEntity.getId());

        return new MethodOutcome(patient.getIdElement());

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
    @Read(version = true)
    public Patient readPatientById(@IdParam IdType id) {

        Long patientId;

        try {
            patientId = Long.parseLong(id.getIdPart());
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("Invalid with ID " + id.getIdPart() + " not found");
        }


        System.out.println("Reading Patient by ID: " + patientId);

        PatientEntity entity = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + patientId + " not found"));

        Patient patient = (Patient) fhirContext
                .newJsonParser()
                .parseResource(entity.getResource());

        // 🔑 Always set the FHIR id before returning
        patient.setId("Patient/" + entity.getId());

        return patient;
//        return (Patient) fhirContext.newJsonParser()
//                .parseResource(patientEntity.getResource());
    }

    /**
     * The "@Update" annotation indicates that this method supports replacing an existing
     * resource (by ID) with a new instance of that resource.
     *
     * @param id      This is the ID of the patient to update
     * @param patient This is the actual resource to save
     * @return This method returns a "MethodOutcome"
     */
//    @Update()
//    public MethodOutcome updatePatient(@IdParam IdType id, @ResourceParam Patient patient) {
//        validateResource(patient);
//
//        Long idHandler;
//
//        try{
//            idHandler = id.getIdPartAsLong();
//        } catch (InvalidRequestException e) {
//            throw new InvalidRequestException("Invalid ID " + id.getValue() + " - Must be numeric");
//        }
//
//        /*
//         * Throw an exception (HTTP 404) if the ID is not known
//         */
//        if(!myIdToPatientVersions.containsKey(idHandler)){
//            throw new ResourceNotFoundException(id);
//        }
//
//        addNewVersion(patient, idHandler);
//
//        return new MethodOutcome();
//    }

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
                    .setDiagnostics("No Family name [provided, Patient resources must have at least one family name.");
            throw new UnprocessableEntityException(fhirContext,  outcome);
        }
    }
}

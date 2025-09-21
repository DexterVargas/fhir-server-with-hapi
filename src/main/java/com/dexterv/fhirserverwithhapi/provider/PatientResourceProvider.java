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
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.zip.DataFormatException;

//@Service
public class PatientResourceProvider implements IResourceProvider {

    @Autowired
    private PatientRepository patientRepository;

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
        patientEntity.setResource(json);
        patientRepository.save(patientEntity);

        // Set FHIR ID from DB ID
        patient.setId("Patient/" + patientEntity.getId());

        return new MethodOutcome(patient.getIdElement());

    }

    /**
     * The "@Search" annotation indicates that this method supports the search operation. You may have many different method annotated with this annotation, to support many different search criteria.
     * This example searches by family name.
     *
     * @param theFamilyName This operation takes one parameter which is the search criteria. It is annotated with the "@Required" annotation. This annotation takes one argument, a string containing the name of
     *                      the search criteria. The datatype here is StringDt, but there are other possible parameter types depending on the specific search criteria.
     * @return This method returns a list of Patients. This list may contain multiple matching resources, or it may also be empty.
     */
//    @Search()
//    public List<Patient> findPatientsByName(@RequiredParam(name = Patient.SP_FAMILY) StringType theFamilyName) {
//        LinkedList<Patient> res = new  LinkedList<Patient>();
//
//        for (Deque<Patient> nextPatientList : myIdToPatientVersions.values()) {
//            Patient nextPatient = nextPatientList.getLast();
//            NAMELOOP:
//            for (HumanName nextHumanName : nextPatient.getName()) {
//                String nextFamilyName = nextHumanName.getFamily().toLowerCase();
//                if (theFamilyName.getValue().equals(nextFamilyName) || nextFamilyName.contains(theFamilyName.getValue())) {
//                    res.add(nextPatient);
//                    break NAMELOOP;
//                }
//            }
//        }
//        System.out.println("Found " + res.size() + " patients");
//        return res;
//    }

    @Search
//    public List<Patient> searchPatient(){
//        LinkedList<Patient> list = new LinkedList<Patient>();
//
//        for(Deque<Patient> versions : myIdToPatientVersions.values()){
//            Patient nextPatient = versions.getLast();
//
//            list.add(nextPatient);
//        }
//
//        return list;
//    }

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

        Long patientId = Long.parseLong(id.getIdPart());
        System.out.println("Reading Patient by ID: " + patientId);

        PatientEntity patientEntity = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with ID " + patientId + " not found"));

        return (Patient) fhirContext.newJsonParser()
                .parseResource(patientEntity.getResource());
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
            throw new UnprocessableEntityException(FhirContext.forR5Cached(),  outcome);
        }
    }
}

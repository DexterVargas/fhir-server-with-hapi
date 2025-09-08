package com.dexterv.fhirserverwithhapi.provider;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientResourceProvider implements IResourceProvider {

    // you must return the type of resource this provider serves:
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }
    /**
     * This map has a resource ID as a key, and each key maps to a Deque list containing all versions of the resource with that ID.
     */
    private Map<Long, Deque<Patient>> myIdToPatientVersions = new HashMap <Long, Deque<Patient>>();

    /**
     * Constructor, which pre-populates the provider with one resource instance.
     */
    private long myNextId = 1;

    /**
     * Constructor, which pre-populates the provider with one resource instance.
     */
    public PatientResourceProvider() {
        long resourceId = myNextId++;

        Patient patient = new Patient();
        patient.setId(Long.toString(resourceId));
        patient.addIdentifier();
        patient.getIdentifier().getFirst().setSystem("urn:uuid:" + resourceId);
        patient.getIdentifier().getFirst().setValue("00002");
        patient.addName().setFamily("Test");
        patient.getName().getFirst().addGiven("PatientOne");
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);

        LinkedList<Patient> list = new LinkedList<>();

        list.add(patient);

        FhirContext ctx = FhirContext.forR5();
        // Convert to JSON String
        String encoded = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);

        System.out.println(encoded);
        System.out.println(myIdToPatientVersions);
        System.out.println(myNextId);

        myIdToPatientVersions.put(resourceId, list);
    }

    /**
     * Stores a new version of the patient in memory so that it can be retrieved later.
     *
     * @param patient The patient resource to store
     * @param Id      The ID of the patient to retrieve
     */
    public void addNewVersion(Patient patient,  long Id) {
        if(!myIdToPatientVersions.containsKey(Id)) {
            myIdToPatientVersions.put(Id, new LinkedList<>());
        }

        patient.getMeta().setLastUpdatedElement(InstantType.withCurrentTime());

        Deque<Patient> existingVersions = myIdToPatientVersions.get(Id);

        // We just use the current number of versions as the next version number
        String newVersion = Integer.toString(existingVersions.size());

        // Create an ID with the new version and assign it back to the resource
        IdType newId = new IdType("Patient", Long.toString(Id), newVersion);
        patient.setId(newId);

        existingVersions.add(patient);
    }

    @Create()
    public MethodOutcome createPatient(@ResourceParam Patient patient) {
        validateResource(patient);

        // Here we are just generating IDs sequentially
        long id = myNextId++;

        addNewVersion(patient, id);

        // Let the caller know the ID of the newly created resource
        return new MethodOutcome(new IdType(id));
    }

    @Search
    public List<Patient> searchPatient(){
        LinkedList<Patient> list = new LinkedList<Patient>();

        for(Deque<Patient> versions : myIdToPatientVersions.values()){
            Patient nextPatient = versions.getLast();

            list.add(nextPatient);
        }

        return list;
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
            throw new UnprocessableEntityException(FhirContext.forR5Cached(),  outcome);
        }
    }
}

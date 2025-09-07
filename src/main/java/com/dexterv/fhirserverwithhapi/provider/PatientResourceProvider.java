package com.dexterv.fhirserverwithhapi.provider;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Patient;

import java.util.*;

public class PatientResourceProvider implements IResourceProvider {
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return null;
    }
    /**
     * This map has a resource ID as a key, and each key maps to a Deque list containing all versions of the resource with that ID.
     */
    private Map<Long, Deque<Patient>> myIdToPatientVersions = new HashMap <Long, Deque<Patient>>();

    /**
     * Constructor, which pre-populates the provider with one resource instance.
     */
    private long myNextId = 1;


}

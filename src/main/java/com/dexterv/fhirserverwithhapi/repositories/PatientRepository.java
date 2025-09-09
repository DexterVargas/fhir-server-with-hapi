package com.dexterv.fhirserverwithhapi.repositories;

import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
}

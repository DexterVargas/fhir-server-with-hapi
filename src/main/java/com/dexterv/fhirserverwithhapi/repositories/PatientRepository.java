package com.dexterv.fhirserverwithhapi.repositories;

import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
}

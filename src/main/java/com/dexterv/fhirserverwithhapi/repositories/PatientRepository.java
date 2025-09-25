package com.dexterv.fhirserverwithhapi.repositories;

import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findTopByResourceIdOrderByVersionDesc(Long resourceId);
    List<PatientEntity> findAllByResourceIdOrderByVersionDesc(Long resourceId);
}

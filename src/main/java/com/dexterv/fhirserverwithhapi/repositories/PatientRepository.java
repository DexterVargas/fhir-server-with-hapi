package com.dexterv.fhirserverwithhapi.repositories;

import com.dexterv.fhirserverwithhapi.domain.entities.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    @Query(value = "SELECT nextval('resource_seq')", nativeQuery = true)
    Long getNextResourceId();

    Optional<PatientEntity> findTopByResourceIdOrderByVersionDesc(Long resourceId);
    List<PatientEntity> findAllByResourceIdOrderByVersionDesc(Long resourceId);
    Optional<PatientEntity> findByResourceIdAndVersion(Long resourceId, Integer version);
}

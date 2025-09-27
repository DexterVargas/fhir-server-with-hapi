package com.dexterv.fhirserverwithhapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="patients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", updatable = false, nullable = false)
    private Long id;  // internal DB PK (never exposed to FHIR)

    @Column(name="resource_id", updatable = false, nullable = false)
    private Long resourceId; // FHIR logical id (Patient/123)

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    private LocalDateTime lastUpdated;

    @Column(name="resource", columnDefinition = "TEXT", nullable = false)
    private String resource;

}

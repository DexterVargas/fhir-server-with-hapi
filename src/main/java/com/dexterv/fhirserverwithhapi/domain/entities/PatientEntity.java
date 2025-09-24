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
    private Long id; // Internal DB ID

    @Column(name="resourceId", unique = true, updatable = true)
    private Long resourceId;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    private LocalDateTime lastUpdated;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resource;
}

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
    private Long id; // Internal DB ID and act as a resourceId

    @Column(name="resource_id", nullable = false, updatable = false, unique = true)
    private Long resourceId;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    private LocalDateTime lastUpdated;

    @Column(name="resource", columnDefinition = "TEXT", nullable = false)
    private String resource;

    // --- Lifecycle hook ---
    @PostPersist
    public void assignResourceId() {
        if (resourceId == null) {
            this.resourceId = this.id;
        }
    }
}

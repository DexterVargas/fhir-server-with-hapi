package com.dexterv.fhirserverwithhapi.domain.entities;

import jakarta.persistence.*;
import lombok.*;

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
    private Long id;

    @Version
    @Column(name="version", nullable = false)
    private Integer version;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resource;
}

package com.dexterv.fhirserverwithhapi.domain.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="patients")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String resource;

    public PatientEntity() {
    }

    public PatientEntity(String resource, Long id) {
        this.resource = resource;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PatientEntity patientEntity = (PatientEntity) o;
        return Objects.equals(id, patientEntity.id) && Objects.equals(resource, patientEntity.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resource);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", resource='" + resource + '\'' +
                '}';
    }
}


# Custom FHIR Server with HAPI FHIR

This project is a **custom-built FHIR server** using [HAPI FHIR](https://hapifhir.io/).
It provides a foundation for working with **FHIR R5 resources** and integrates with a **PostgreSQL database** through JPA/Hibernate for persistence.

---

## Features Implemented

* **FHIR Server Setup**

    * Configured with `FhirRestfulServer` servlet
    * Validation support enabled (resource validation before persistence)

* **Patient Resource Provider**

    * `@Create` – Store new `Patient` resources in the database
    * `@Read` – Retrieve a patient by ID
    * `@Update` – Update existing patients (with versioning support)
    * `@History` – Track patient history across versions
    * `@Search` – Search patients by supported parameters

* **Persistence Layer**

    * `PatientEntity` – JPA entity mapped to the `patients` table
    * `PatientMapper` – Conversion between FHIR `Patient` resource and database entity
    * `PatientRepository` – Spring Data repository for persistence

---

## Project Structure

```
src/main/java/com.dexterv.fhirserverwithhapi
│
├── config/
│   └── ValidationConfig.java    # Resource validation setup
│
├── domain/
│   └── entities/
│          └── PatientEntity.java
│
│── mapper/
│       └── PatientMapper.java
│
├── providers/
│   └── PatientResourceProvider.java # Implements CRUD + Search + History
│
│repository/
│       └── PatientRepository.java
├── servlet/
│   └── FhirRestfulServer.java   # Servlet configuration
```

---

## Getting Started

### Prerequisites

* Java 17+ or 21
* Maven
* Docker (for PostgreSQL database)

### Run Database

```bash
docker compose up
```

### Run Application

```bash
./mvnw spring-boot:run
```

Server should now be available at:
`http://localhost:8484/fhir`

---

## Example Requests

### Create Patient

```http
POST /fhir/Patient
Content-Type: application/fhir+json

{
  "resourceType": "Patient",
  "name": [{ "family": "Doe", "given": ["John"] }],
  "gender": "male",
  "birthDate": "1990-01-01"
}
```

### Read Patient

```http
GET /fhir/Patient/{id}
```

### Search ALL Patients

```http
GET /fhir/Patient
```

### Search Patient with version

```http
GET /fhir/Patient/{id}/_history/2
```
---

## TODO

* Add more resource providers (e.g., `Observation`, `Encounter`, `Practitioner`)
* Enhance search parameters for Patient (e.g., birthDate, identifiers)
* Add authentication and authorization layer
* Write integration and unit tests

---

## References

* [HAPI FHIR Documentation](https://hapifhir.io/hapi-fhir/docs/)
* [FHIR R5 Specification](https://hl7.org/fhir/)
* [resource-providers](https://hapifhir.io/hapi-fhir/docs/server_plain/resource_providers.html#resource-providers)
* [hapi-fhir-server](https://hapifhir.io/hapi-fhir/apidocs/hapi-fhir-server/index-all.html)
* [rest_operations](https://hapifhir.io/hapi-fhir/docs/server_plain/rest_operations.html)
---

## License

This project is open-source and available under the MIT License.

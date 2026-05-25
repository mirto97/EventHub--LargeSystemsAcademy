# EventHub — Large System Academy
> File di contesto del progetto. Da reinserire ad ogni nuova sessione.

---

## Panoramica del progetto

Piattaforma di gestione eventi con prenotazione biglietti e feedback.  
Progetto accademico con roadmap da 15 giorni.

**Deliverable finali:** repo Git + Swagger UI + demo live

---

## Stack tecnico

| Componente | Scelta |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Packaging | WAR |
| ORM | Spring Data JPA + Hibernate |
| Database | **MySQL** (locale, no Docker) |
| Mapping DTO | MapStruct 1.6.3 |
| Boilerplate | Lombok 1.18.36 |
| Auth | JWT (jjwt 0.11.5) — Spring Security commentata per ora |
| API Docs | springdoc-openapi 3.0.3 |
| Build | Maven |

**GroupId:** `com.academy`  
**ArtifactId:** `eventhub`

---

## Database

- **Tipo:** MySQL installato localmente (no Docker)
- **Nome DB:** `eventhub`
- **URL:** `jdbc:mysql://localhost:3306/eventhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- **Username:** `root` / **Password:** `1234`
- **Dialect:** `org.hibernate.dialect.MySQLDialect`
- **ddl-auto:** `update`
- **Query:** si usa **JPQL** (no SQL nativo), quindi cambiare DB in futuro è banale

> Nota: PostgreSQL + Docker è una opzione futura. Cambio richiede solo: swap dipendenza pom.xml + 3 righe application.properties + docker-compose.yml. Il codice Java non cambia.

---

## application.properties (configurazione attuale)

```properties
spring.application.name=eventhub

spring.datasource.url=jdbc:mysql://localhost:3306/eventhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Swagger — RICORDA: mettere enabled=true quando si testano le API
springdoc.swagger-ui.path=/swagger
springdoc.swagger-ui.enabled=false
springdoc.api-docs.path=/apidocs
springdoc.api-docs.enabled=false

spring.jackson.default-property-inclusion=non_null

logging.level.org.hibernate.SQL=trace
logging.level.org.hibernate.orm.jdbc.bind=trace
logging.level.org.springframework.security=debug
logging.level.root=warn
```

> ⚠️ Swagger è disabilitato — ricordarsi di riabilitare `enabled=true` su entrambi quando si inizia a testare.

---

## Entità e modello dati

### Entità previste (9 originali → 8 effettive)

| Entità | Note |
|---|---|
| `User` | Autenticazione + ruolo + status |
| `UserProfile` | Dati personali, @OneToOne con User |
| ~~`Role`~~ | **Eliminata** — sostituita da enum interno a User |
| `Venue` | Sede dell'evento |
| `Event` | Evento, collegato a Venue e a User (organizer) |
| `Tag` | Many-to-many con Event |
| `Speaker` | Many-to-many con Event |
| `Ticket` | Join entity tra User ed Event, con campi extra |
| `Feedback` | Join entity tra User ed Event, con rating e commento |

---

## Decisioni architetturali importanti

### Ruoli — enum, non entità
- L'entità `Role` è stata **eliminata**
- I ruoli sono un **enum interno a `User`**: `USER`, `ORGANIZER`, `ADMIN`
- Ogni utente ha **esattamente un ruolo** (campo singolo, non Set)
- I ruoli sono **separati e non gerarchici** — ORGANIZER non eredita da USER, ADMIN non eredita da ORGANIZER
- L'unica relazione tra ruoli: ADMIN può promuovere un USER a ORGANIZER (cambia il campo `role`)

```java
@Enumerated(EnumType.STRING)
private Role role;

public enum Role { USER, ORGANIZER, ADMIN }
```

### Status utente — enum, non booleano
- Invece di `boolean active`, si usa un enum `Status`
- Più espressivo e estendibile in futuro

```java
@Enumerated(EnumType.STRING)
private Status status = Status.ACTIVE;

public enum Status { ACTIVE, BANNED }
```

### UserProfile — composizione, non ereditarietà
- `UserProfile` NON estende `User`
- Relazione **@OneToOne**: User "ha un" UserProfile, non "è un" UserProfile
- `User` gestisce autenticazione; `UserProfile` gestisce dati personali
- `UserProfile` ha la FK (`user_id`)

### Lombok su @Entity
- **Non usare `@Data`** sulle entity JPA — genera `equals()`/`hashCode()` su tutti i campi incluse le relazioni lazy → rischio `LazyInitializationException` e loop infiniti
- Usare sempre `@Getter` + `@Setter` + `@NoArgsConstructor` + `@AllArgsConstructor` separati

---

## Entity scritte finora

### User.java

```java
package com.academy.eventhub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Schema(description = "Entità che rappresenta un utente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID univoco generato automaticamente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private int id;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
    public enum Role { USER, ORGANIZER, ADMIN }

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    public enum Status { ACTIVE, BANNED }

    // DA AGGIUNGERE lato inverso:
    // @OneToOne(mappedBy = "user")
    // private UserProfile profile;
}
```

### UserProfile.java

```java
package com.academy.eventhub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Schema(description = "Entità che rappresenta il profilo pubblico di un utente")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID univoco generato automaticamente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private int id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    @Schema(description = "Nome dell'utente", example = "Mario")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    @Schema(description = "Cognome dell'utente", example = "Rossi")
    private String lastName;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Breve biografia dell'utente", example = "Appassionato di eventi tech")
    private String bio;

    @Schema(description = "Città dell'utente", example = "Roma")
    private String city;

    @Schema(description = "URL della foto profilo", example = "https://example.com/foto.jpg")
    private String photoUrl;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "Utente associato al profilo", accessMode = Schema.AccessMode.READ_ONLY)
    private User user;
}
```

---

## Regole di business (dalla traccia originale)

1. Un utente non può prenotare il proprio evento
2. Non si può prenotare un evento già al completo (posti esauriti)
3. Non si può lasciare feedback se non si ha un biglietto per quell'evento
4. Un evento non può iniziare nel passato
5. La data di fine evento deve essere successiva alla data di inizio
6. Un utente bannato non può prenotare né creare eventi
7. Solo ADMIN può promuovere un USER a ORGANIZER
8. Solo ORGANIZER può creare/modificare/eliminare i propri eventi
9. Solo ADMIN può gestire Venue, Speaker e Tag

---

## Relazioni tra entità (ER)

- `User` ←→ `UserProfile` : OneToOne
- `User` → `Event` : OneToMany (un organizer crea N eventi)
- `User` → `Ticket` : OneToMany
- `User` → `Feedback` : OneToMany
- `Venue` → `Event` : OneToMany
- `Event` ←→ `Tag` : ManyToMany
- `Event` ←→ `Speaker` : ManyToMany
- `Event` → `Ticket` : OneToMany
- `Event` → `Feedback` : OneToMany

---

## TODO / Prossimi step

- [ ] Completare entity: `Venue`, `Event`, `Tag`, `Speaker`, `Ticket`, `Feedback`
- [ ] Aggiungere lato inverso `@OneToOne(mappedBy = "user")` in `User`
- [ ] Creare i Repository (Spring Data JPA)
- [ ] Creare i DTO + MapStruct mapper
- [ ] Creare i Service
- [ ] Creare i Controller REST
- [ ] Attivare Spring Security + JWT
- [ ] Abilitare Swagger (`enabled=true`)
- [ ] Frontend vanilla JS


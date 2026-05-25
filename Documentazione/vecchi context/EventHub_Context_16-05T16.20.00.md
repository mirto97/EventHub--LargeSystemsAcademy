# EventHub — Large System Academy
> File di contesto del progetto. Da reinserire ad ogni nuova sessione.

---

## Panoramica del progetto

Piattaforma di gestione eventi con prenotazione biglietti e feedback.
Progetto accademico con roadmap da 15 giorni.

**Deliverable finali:** repo Git + Swagger UI + demo live

---

## Regole fondamentali del progetto

- Il file `EventHub_Academy_2026.md` è LEGGE — qualsiasi deviazione va approvata esplicitamente
- MySQL al posto di PostgreSQL: **concesso**
- MapStruct: **concesso**
- HTTP Basic al posto di JWT: **requisito del file, rispettato**
- Niente framework frontend (no React/Vue/Angular): **requisito del file, rispettato**

---

## Stack tecnico

| Componente | Scelta |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Packaging | WAR |
| ORM | Spring Data JPA + Hibernate |
| Database | **MySQL** (locale, no Docker) — PostgreSQL + Docker in futuro |
| Mapping DTO | MapStruct 1.6.3 |
| Boilerplate | Lombok 1.18.36 |
| Auth | Spring Security 7 — HTTP Basic + BCrypt |
| API Docs | springdoc-openapi 3.0.3 |
| Build | Maven |

**GroupId:** `com.academy`
**ArtifactId:** `eventhub`

---

## Struttura dei package

```
com.academy.eventhub
├── api/              ← controller REST
├── dto/              ← tutti i DTO (flat, no sottocartelle)
├── entity/           ← entità JPA
├── exception/        ← eccezioni custom + GlobalExceptionHandler
├── mapper/           ← mapper MapStruct
├── repository/       ← repository Spring Data JPA
├── security/         ← CustomUserDetails, CustomUserDetailsService, SecurityConfig
└── service/          ← logica di business
```

---

## Database

- **Tipo:** MySQL installato localmente (no Docker)
- **Nome DB:** `eventhub`
- **URL:** `jdbc:mysql://localhost:3306/eventhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
- **Username:** `root` / **Password:** `1234`
- **Dialect:** `org.hibernate.dialect.MySQLDialect`
- **ddl-auto:** `update`
- **Query:** JPQL (no SQL nativo)

---

## application.properties

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

# Swagger — abilitare enabled=true su entrambi per testare
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

---

## Decisioni architetturali

### Ruoli — enum interno a User
```java
@Enumerated(EnumType.STRING)
private Role role;
public enum Role { USER, ORGANIZER, ADMIN }
```
- Ruoli separati e non gerarchici
- ADMIN può promuovere USER → ORGANIZER

### Status utente — enum
```java
@Enumerated(EnumType.STRING)
private Status status = Status.ACTIVE;
public enum Status { ACTIVE, BANNED }
```

### Ticket status — enum separato da User.Status
```java
public enum TicketStatus { ACTIVE, CANCELLED }
```
Tenuti separati perché semanticamente diversi (BANNED ≠ CANCELLED).

### UserProfile — composizione, non ereditarietà
- `@OneToOne` con User; UserProfile ha la FK `user_id`

### Lombok su @Entity
- **Mai `@Data`** sulle entity JPA (rischio LazyInitializationException)
- Usare `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`

### DTO
- `@Data` sui DTO è OK (nessun rischio JPA)
- Separati in Request (validazione) e Response (@Schema)
- `@Schema` solo sui DTO, mai sulle entity

### Prezzi
- `BigDecimal` per i prezzi (evita errori floating point)

---

## Entità

### User.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToOne(mappedBy = "user")
    private UserProfile profile;
}
```

### UserProfile.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String city;
    private String photoUrl;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

### Venue.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il nome della sede è obbligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Column(nullable = false)
    private String address;

    @Min(value = 1, message = "La capienza deve essere almeno 1")
    @Column(nullable = false)
    private int capacity;
}
```

### Tag.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il nome del tag è obbligatorio")
    @Column(nullable = false, unique = true)
    private String name;
}
```

### Speaker.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Speaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Column(nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String photoUrl;
}
```

### Event.java
```java
package com.academy.eventhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il titolo è obbligatorio")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "La data di fine è obbligatoria")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @NotNull(message = "Il prezzo standard è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal standardPrice;

    @NotNull(message = "Il prezzo VIP è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal vipPrice;

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToMany
    @JoinTable(
        name = "event_tag",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "event_speaker",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "speaker_id")
    )
    private List<Speaker> speakers = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();
}
```

> Nota: `@AssertTrue isEndDateAfterStartDate()` rimosso dall'entity — la validazione cross-field è spostata su `EventRequestDTO`.

### Ticket.java
```java
package com.academy.eventhub.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Il tipo di biglietto è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;
    public enum TicketType { STANDARD, VIP }

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ACTIVE;
    public enum TicketStatus { ACTIVE, CANCELLED }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
```

### Feedback.java
```java
package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
```

---

## Relazioni tra entità

- `User` ←→ `UserProfile` : OneToOne bidirezionale
- `User` → `Event` : OneToMany (organizer)
- `User` → `Ticket` : OneToMany
- `User` → `Feedback` : OneToMany
- `Venue` → `Event` : OneToMany
- `Event` ←→ `Tag` : ManyToMany (join table: `event_tag`)
- `Event` ←→ `Speaker` : ManyToMany (join table: `event_speaker`)
- `Event` → `Ticket` : OneToMany (cascade ALL)
- `Event` → `Feedback` : OneToMany (cascade ALL)

---

## DTO (package `com.academy.eventhub.dto`)

Tutti flat nella stessa cartella, divisi in Request e Response.

| File | Tipo | Note |
|---|---|---|
| `SignupRequestDTO` | Request | email, password |
| `UserResponseDTO` | Response | id, email, role, status |
| `UserProfileRequestDTO` | Request | firstName, lastName, bio, city, photoUrl |
| `UserProfileResponseDTO` | Response | id + campi profilo |
| `VenueRequestDTO` | Request | name, address, capacity |
| `VenueResponseDTO` | Response | id, name, address, capacity |
| `TagRequestDTO` | Request | name |
| `TagResponseDTO` | Response | id, name |
| `SpeakerRequestDTO` | Request | firstName, lastName, bio, photoUrl |
| `SpeakerResponseDTO` | Response | id + campi speaker |
| `EventRequestDTO` | Request | tutti i campi + venueId, tagIds, speakerIds + @AssertTrue date |
| `EventResponseDTO` | Response | tutti i campi + availableSeats (calcolato) + oggetti annidati |
| `TicketRequestDTO` | Request | type, eventId |
| `TicketResponseDTO` | Response | id, type, price, status, eventId, eventTitle |
| `FeedbackRequestDTO` | Request | rating, comment, eventId |
| `FeedbackResponseDTO` | Response | id, rating, comment, user (UserResponseDTO), eventId, eventTitle |

Regole DTO:
- `@Schema` solo sui DTO Response
- `@AssertTrue isEndDateAfterStartDate()` su `EventRequestDTO` (non sull'entity)
- Il `price` non è in `TicketRequestDTO` — lo imposta il service in base al tipo
- `availableSeats` in `EventResponseDTO` è calcolato dal service: `venue.capacity - ticketAttivi`

---

## Mapper (package `com.academy.eventhub.mapper`)

| File | Note |
|---|---|
| `UserMapper` | toResponseDTO |
| `UserProfileMapper` | toResponseDTO, toEntity (ignora id e user), updateEntity (ignora id e user) |
| `VenueMapper` | toResponseDTO, toEntity (ignora id), updateEntity (ignora id) |
| `TagMapper` | toResponseDTO, toEntity (ignora id) |
| `SpeakerMapper` | toResponseDTO, toEntity (ignora id), updateEntity (ignora id) |
| `EventMapper` | toResponseDTO (ignora availableSeats), toEntity (ignora id/venue/organizer/tags/speakers/tickets/feedbacks), updateEntity (stessi ignore) — usa VenueMapper, UserMapper, TagMapper, SpeakerMapper |
| `TicketMapper` | toResponseDTO — mappa event.id → eventId, event.title → eventTitle |
| `FeedbackMapper` | toResponseDTO — mappa event.id → eventId, event.title → eventTitle — usa UserMapper |

---

## Repository (package `com.academy.eventhub.repository`)

### UserRepository
```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

### UserProfileRepository
```java
Optional<UserProfile> findByUserId(int userId);
```

### VenueRepository
```java
boolean existsByName(String name);
```

### TagRepository
```java
Optional<Tag> findByName(String name);
boolean existsByName(String name);
```

### SpeakerRepository
- solo CRUD base

### EventRepository
```java
List<Event> findByOrganizerId(int organizerId);
List<Event> findByStartDateAfter(LocalDateTime date);
List<Event> findByVenueId(int venueId);
List<Event> findByTagsContaining(Tag tag);
List<Event> findByStartDateAfterAndTagsContaining(LocalDateTime date, Tag tag);
```

### TicketRepository
```java
List<Ticket> findByUserId(int userId);
List<Ticket> findByEventId(int eventId);
boolean existsByUserIdAndEventId(int userId, int eventId);
int countByEventIdAndStatus(int eventId, Ticket.TicketStatus status);
Optional<Ticket> findByUserIdAndEventIdAndStatus(int userId, int eventId, Ticket.TicketStatus status);
```

### FeedbackRepository
```java
List<Feedback> findByEventId(int eventId);
List<Feedback> findByUserId(int userId);
boolean existsByUserIdAndEventId(int userId, int eventId);

@Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
Double findAverageRatingByEventId(@Param("eventId") int eventId);
```

---

## Service (package `com.academy.eventhub.service`)

### AuthService
- `signup(SignupRequestDTO)` → controlla email duplicata, encode password, ruolo USER, status ACTIVE

### UserService
- `getAllUsers()`, `getUserById(int)`
- `promoteUser(int)` → solo da USER a ORGANIZER
- `banUser(int)` → non applicabile ad ADMIN
- `reactivateUser(int)`

### UserProfileService
- `getProfileByUserId(int)`
- `createProfile(int userId, UserProfileRequestDTO)` → verifica profilo non già esistente
- `updateProfile(int userId, UserProfileRequestDTO)`

### VenueService
- CRUD completo + controllo nome duplicato

### TagService
- `getAllTags()`, `createTag()`, `deleteTag()` + controllo nome duplicato

### SpeakerService
- CRUD completo

### EventService
- `getAllEvents()`, `getEventById()`, `getEventsByOrganizer(int)`
- `createEvent(EventRequestDTO, int organizerId)` → fetch venue/tags/speakers da repo, controllo ban
- `updateEvent(int eventId, EventRequestDTO, int requestingUserId)` → controllo ownership/admin
- `deleteEvent(int eventId, int requestingUserId)` → controllo ownership/admin
- `toResponseWithSeats(Event)` → metodo privato che calcola availableSeats
- `checkOwnershipOrAdmin(Event, int)` → metodo privato
- `resolveTags(List<Integer>)` e `resolveSpeakers(List<Integer>)` → metodi privati

### TicketService
- `bookTicket(TicketRequestDTO, int userId)` → controlla: ban, evento futuro, non proprio evento, no doppia prenotazione, posti disponibili; imposta prezzo dal tipo
- `cancelTicket(int ticketId, int userId)` → controlla ownership, non già cancellato, evento non ancora iniziato
- `getUserTickets(int userId)`
- `getEventParticipants(int eventId, int requestingUserId)` → solo owner o admin

### FeedbackService
- `leaveFeedback(FeedbackRequestDTO, int userId)` → controlla: evento concluso, biglietto attivo presente, no duplicato
- `getEventFeedbacks(int eventId)`
- `getEventRating(int eventId)` → media tramite query JPQL
- `deleteFeedback(int feedbackId)` → solo admin

---

## Eccezioni custom (package `com.academy.eventhub.exception`)

### ResourceNotFoundException
- Estende `RuntimeException`
- Usata per 404

### BusinessException
- Estende `RuntimeException`
- Usata per 409 (violazioni di business)

### GlobalExceptionHandler
- `@RestControllerAdvice`
- Gestisce: `ResourceNotFoundException` → 404, `BusinessException` → 409, `MethodArgumentNotValidException` → 400 con mappa field→message, `Exception` generica → 500

---

## Security (package `com.academy.eventhub.security`)

**Tipo:** HTTP Basic + BCrypt (requisito del file Academy)
**Stato:** dipendenza Spring Security attiva nel pom.xml

### CustomUserDetails
- Implementa `UserDetails`
- Espone `int id` (per evitare query extra nei controller)
- `getAuthorities()` → `ROLE_USER` / `ROLE_ORGANIZER` / `ROLE_ADMIN`
- `isAccountNonLocked()` → false se BANNED
- `isEnabled()` → true solo se ACTIVE

### CustomUserDetailsService
- Implementa `UserDetailsService`
- `loadUserByUsername(email)` → cerca per email, wrappa in `CustomUserDetails`

### SecurityConfig
- HTTP Basic: `Customizer.withDefaults()`
- Stateless (no sessione)
- CSRF disabilitato
- `DaoAuthenticationProvider(userDetailsService)` + `setPasswordEncoder()`
- URL protetti per ruolo (vedi sotto)

#### Matrice autorizzazioni
| Endpoint | Accesso |
|---|---|
| `POST /auth/signup` | pubblico |
| `GET /events`, `GET /events/{id}` | pubblico |
| `GET /events/{id}/feedbacks`, `GET /events/{id}/rating` | pubblico |
| `GET /venues`, `GET /venues/{id}` | pubblico |
| `GET /tags` | pubblico |
| `GET /speakers`, `GET /speakers/{id}` | pubblico |
| `/swagger`, `/apidocs/**`, `/swagger-ui/**` | pubblico |
| `/admin/**` | solo ADMIN |
| `POST /events` | ORGANIZER o ADMIN |
| `PUT /events/{id}`, `DELETE /events/{id}` | ORGANIZER o ADMIN |
| `/events/my`, `/events/{id}/participants` | ORGANIZER o ADMIN |
| `/me/**` | autenticato |
| `/events/{eventId}/book`, `/tickets/**`, `/feedbacks/**` | autenticato |

---

## Controller (package `com.academy.eventhub.api`)

**Stato: DA FARE**

Quando verranno scritti i controller:
- Usare `@AuthenticationPrincipal CustomUserDetails userDetails` per ricavare l'utente autenticato
- Ricavare l'ID con `userDetails.getId()`
- Documentazione Swagger completa su ogni endpoint (`@Operation`, `@ApiResponses`, `@Parameter`)

---

## TODO aggiornato

- [x] Entity complete
- [x] DTO completi
- [x] Mapper MapStruct
- [x] Repository
- [x] Service
- [x] Eccezioni custom + GlobalExceptionHandler
- [x] Security (CustomUserDetails, CustomUserDetailsService, SecurityConfig)
- [ ] **Controller REST** ← prossimo step
- [ ] Test JUnit (EventService, TicketService) — dopo i controller
- [ ] Frontend HTML/CSS/JS vanilla
- [ ] Abilitare Swagger (enabled=true in application.properties)
- [ ] README completo
- [ ] Migrazione PostgreSQL + Docker Compose (quando richiesto)

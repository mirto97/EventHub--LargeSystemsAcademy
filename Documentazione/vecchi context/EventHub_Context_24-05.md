# EventHub — Context Dump
> File di contesto del progetto. Da reinserire ad ogni nuova sessione.
> Ultimo aggiornamento: 24/05/2026

---

## Note operative per Claude

- Scrivere sempre il codice **direttamente in chat** (inline), non come file da scaricare.
- **Non inventare mai** nomi di classi, metodi, campi senza conferma esplicita. Se qualcosa non è documentato qui, chiedere prima di procedere.
- Il file `EventHub_Academy_2026.md` è LEGGE — qualsiasi deviazione va approvata esplicitamente dall'utente.
- Le pagine HTML non sono documentate nel dettaglio — se servono modifiche, chiedere il file prima di procedere.

---

## Stato del progetto

**Progetto completato e consegnato.** Tutto il backend, frontend, test e README sono stati implementati.

Attività future (post-consegna):
- Migrazione da MySQL a PostgreSQL
- Docker Compose

---

## Repository

https://github.com/mirto97/EventHub--LargeSystemsAcademy.git


---

## Regole fondamentali

- Il file `EventHub_Academy_2026.md` è LEGGE
- MySQL al posto di PostgreSQL: **concesso**
- MapStruct: **concesso**
- HTTP Basic + BCrypt: **requisito rispettato**
- Niente framework frontend: **requisito rispettato**

---

## Stack tecnico

| Componente | Scelta |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Packaging | WAR |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL (locale, no Docker) |
| Mapping DTO | MapStruct 1.6.3 |
| Boilerplate | Lombok 1.18.36 |
| Auth | Spring Security 7 — HTTP Basic + BCrypt |
| API Docs | springdoc-openapi 3.0.3 |
| Build | Maven |

**GroupId:** `com.academy`
**ArtifactId:** `eventhub`

---

## Struttura dei package
com.academy.eventhub
├── api/              ← controller REST
├── dto/              ← tutti i DTO (flat, no sottocartelle)
├── entity/           ← entità JPA
├── exception/        ← eccezioni custom + GlobalExceptionHandler
├── mapper/           ← mapper MapStruct
├── repository/       ← repository Spring Data JPA
├── security/         ← CustomUserDetails, CustomUserDetailsService, SecurityConfig
└── service/          ← logica di business

Frontend:
src/main/resources/static/
├── css/style.css
├── html/             ← login, signup, events, event-detail,
│                        my-bookings, profile, organizer-events, admin
└── js/auth.js

Test:
src/test/java/com/academy/eventhub/service/
├── EventServiceTest.java
└── TicketServiceTest.java

---

## application.properties (stato attuale)

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

# Swagger — path custom commentato, usa il default: /swagger-ui/index.html
# springdoc.swagger-ui.path=/swagger
# springdoc.swagger-ui.enabled=false
# springdoc.swagger-ui.path=/swagger-ui.html

# API docs abilitati su path custom
springdoc.api-docs.path=/apidocs
# springdoc.api-docs.enabled=false

spring.jackson.default-property-inclusion=non_null

logging.level.org.hibernate.SQL=trace
logging.level.org.hibernate.orm.jdbc.bind=trace
logging.level.org.springframework.security=debug
logging.level.root=warn

# spring.data.rest.base-path=/api
```

> **Swagger UI** è accessibile su `http://localhost:8080/swagger-ui/index.html`
> **API docs JSON** su `http://localhost:8080/apidocs`

---

## Credenziali demo

| Ruolo | Email | Password |
|---|---|---|
| ADMIN | admin@eventhub.com | 123 |
| ORGANIZER | organizer@eventhub.com | 123 |
| USER | user@eventhub.com | 123 |

---

## DataInitializer.java

Presente ma **completamente commentato**. Va decommentato se si resetta il DB e si vogliono ricreare gli utenti demo automaticamente all'avvio.

```java
package com.academy.eventhub;

// import com.academy.eventhub.entity.User;
// import com.academy.eventhub.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// CommandLineRunner viene eseguito dopo che l'applicazione è partita e il database è pronto.
// public class DataInitializer implements CommandLineRunner {

//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;

//     @Override
//     public void run(String... args) {
//         createUserIfNotExists("user@eventhub.com", User.Role.USER);
//         createUserIfNotExists("organizer@eventhub.com", User.Role.ORGANIZER);
//         createUserIfNotExists("admin@eventhub.com", User.Role.ADMIN);
//     }

//     private void createUserIfNotExists(String email, User.Role role) {
//         if (userRepository.existsByEmail(email))
//             return;

//         User user = new User();
//         user.setEmail(email);
//         user.setPassword(passwordEncoder.encode("123"));
//         user.setRole(role);
//         user.setStatus(User.Status.ACTIVE);

//         userRepository.save(user);
//         System.out.println("Utente creato: " + email + " [" + role + "]");
//     }
// }
```

---

## Decisioni architetturali

- Ruoli: enum interno a `User` — `USER`, `ORGANIZER`, `ADMIN` (non gerarchici)
- Status utente: enum interno a `User` — `ACTIVE`, `BANNED`
- Ticket status: enum separato — `ACTIVE`, `CANCELLED`
- `UserProfile`: `@OneToOne` con `User`; UserProfile ha la FK `user_id`
- **Mai `@Data`** sulle entity JPA — usare `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`
- `@Data` sui DTO è OK
- `@Schema` solo sui DTO Response, mai sulle entity
- `BigDecimal` per i prezzi
- `@RequiredArgsConstructor` su tutte le classi — mai `@Autowired`
- `@AssertTrue isEndDateAfterStartDate()` su `EventRequestDTO`, non sull'entity

---

## Entità (package `com.academy.eventhub.entity`)

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

## DTO (package `com.academy.eventhub.dto`)

Tutti flat. `@Data @NoArgsConstructor @AllArgsConstructor` su tutti.
`@Schema` solo sui Response. Validazione solo sui Request.

| Classe | Tipo | Campi principali |
|---|---|---|
| `SignupRequestDTO` | Request | email, password |
| `UserResponseDTO` | Response | id, email, role (User.Role), status (User.Status) |
| `UserProfileRequestDTO` | Request | firstName, lastName, bio, city, photoUrl |
| `UserProfileResponseDTO` | Response | id, firstName, lastName, bio, city, photoUrl |
| `VenueRequestDTO` | Request | name, address, capacity (Integer) |
| `VenueResponseDTO` | Response | id, name, address, capacity |
| `TagRequestDTO` | Request | name |
| `TagResponseDTO` | Response | id, name |
| `SpeakerRequestDTO` | Request | firstName, lastName, bio, photoUrl |
| `SpeakerResponseDTO` | Response | id, firstName, lastName, bio, photoUrl |
| `EventRequestDTO` | Request | title, description, startDate, endDate, standardPrice, vipPrice, venueId (Integer), tagIds (List\<Integer\>), speakerIds (List\<Integer\>) + `@AssertTrue isEndDateAfterStartDate()` |
| `EventResponseDTO` | Response | id, title, description, startDate, endDate, standardPrice, vipPrice, availableSeats (calcolato), venue (VenueResponseDTO), organizer (UserResponseDTO), tags, speakers |
| `TicketRequestDTO` | Request | type (Ticket.TicketType), eventId (Integer) — ha setter su eventId, usato dal controller |
| `TicketResponseDTO` | Response | id, type, price, status, eventId, eventTitle |
| `FeedbackRequestDTO` | Request | rating (1-5), comment, eventId (Integer) |
| `FeedbackResponseDTO` | Response | id, rating, comment, user (UserResponseDTO), eventId, eventTitle |

---

## Mapper (package `com.academy.eventhub.mapper`)

| Mapper | Note |
|---|---|
| `UserMapper` | toResponseDTO |
| `UserProfileMapper` | toResponseDTO, toEntity (ignora id e user), updateEntity (ignora id e user) |
| `VenueMapper` | toResponseDTO, toEntity (ignora id), updateEntity (ignora id) |
| `TagMapper` | toResponseDTO, toEntity (ignora id) |
| `SpeakerMapper` | toResponseDTO, toEntity (ignora id), updateEntity (ignora id) |
| `EventMapper` | toResponseDTO (ignora availableSeats), toEntity e updateEntity (ignorano id/venue/organizer/tags/speakers/tickets/feedbacks) — usa VenueMapper, UserMapper, TagMapper, SpeakerMapper |
| `TicketMapper` | toResponseDTO — mappa event.id→eventId, event.title→eventTitle |
| `FeedbackMapper` | toResponseDTO — mappa event.id→eventId, event.title→eventTitle — usa UserMapper |

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
Solo CRUD base (estende JpaRepository).

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

## Eccezioni (package `com.academy.eventhub.exception`)

- `ResourceNotFoundException` → 404
- `BusinessException` → 409
- `GlobalExceptionHandler` (@RestControllerAdvice):
  - `ResourceNotFoundException` → 404
  - `BusinessException` → 409
  - `MethodArgumentNotValidException` → 400 con mappa field→message
  - `Exception` generica → 500
  - Tutti i body hanno: `timestamp`, `status`, `error`

---

## Security (package `com.academy.eventhub.security`)

- HTTP Basic con `Customizer.withDefaults()`, stateless, CSRF disabilitato
- `DaoAuthenticationProvider(userDetailsService)` + `BCryptPasswordEncoder`
- `CustomUserDetails`: espone `int id`, `getAuthorities()` → `ROLE_USER/ORGANIZER/ADMIN`, `isAccountNonLocked()` → false se BANNED, `isEnabled()` → true solo se ACTIVE
- `CustomUserDetailsService`: `loadUserByUsername(email)`

### Matrice autorizzazioni
| Endpoint | Accesso |
|---|---|
| `POST /auth/signup` | pubblico |
| `GET /events`, `GET /events/{id}` | pubblico |
| `GET /events/{id}/feedbacks`, `GET /events/{id}/rating` | pubblico |
| `GET /venues`, `GET /venues/{id}` | pubblico |
| `GET /tags` | pubblico |
| `GET /speakers`, `GET /speakers/{id}` | pubblico |
| `/swagger-ui/**`, `/apidocs/**` | pubblico |
| `/admin/**` | solo ADMIN |
| `POST/PUT/DELETE /events` | ORGANIZER o ADMIN |
| `GET /events/my`, `GET /events/{id}/participants` | ORGANIZER o ADMIN |
| `/me/**` | autenticato |
| `POST /events/{eventId}/book`, `/tickets/**`, `/feedbacks/**` | autenticato |

---

## Service (package `com.academy.eventhub.service`)

### Firme dei metodi — tutti i service

**AuthService**
- `signup(SignupRequestDTO dto)` → `UserResponseDTO`

**UserService**
- `getAllUsers()` → `List<UserResponseDTO>`
- `getUserById(int id)` → `UserResponseDTO`
- `promoteUser(int id)` → `UserResponseDTO` — solo da USER a ORGANIZER
- `banUser(int id)` → `UserResponseDTO` — non applicabile ad ADMIN
- `reactivateUser(int id)` → `UserResponseDTO`

**UserProfileService**
- `getProfileByUserId(int userId)` → `UserProfileResponseDTO`
- `createProfile(int userId, UserProfileRequestDTO dto)` → `UserProfileResponseDTO`
- `updateProfile(int userId, UserProfileRequestDTO dto)` → `UserProfileResponseDTO`

**VenueService**
- `getAllVenues()` → `List<VenueResponseDTO>`
- `getVenueById(int id)` → `VenueResponseDTO`
- `createVenue(VenueRequestDTO dto)` → `VenueResponseDTO` — controlla nome duplicato
- `updateVenue(int id, VenueRequestDTO dto)` → `VenueResponseDTO`
- `deleteVenue(int id)` → `void`

**TagService**
- `getAllTags()` → `List<TagResponseDTO>`
- `createTag(TagRequestDTO dto)` → `TagResponseDTO` — controlla nome duplicato
- `deleteTag(int id)` → `void`

**SpeakerService**
- `getAllSpeakers()` → `List<SpeakerResponseDTO>`
- `getSpeakerById(int id)` → `SpeakerResponseDTO`
- `createSpeaker(SpeakerRequestDTO dto)` → `SpeakerResponseDTO`
- `updateSpeaker(int id, SpeakerRequestDTO dto)` → `SpeakerResponseDTO`
- `deleteSpeaker(int id)` → `void`

**EventService**
- `getAllEvents(LocalDateTime date, Integer tagId, Integer venueId)` → `List<EventResponseDTO>`
- `getEventById(int id)` → `EventResponseDTO`
- `getEventsByOrganizer(int organizerId)` → `List<EventResponseDTO>`
- `createEvent(EventRequestDTO dto, int organizerId)` → `EventResponseDTO`
- `updateEvent(int eventId, EventRequestDTO dto, int requestingUserId)` → `EventResponseDTO`
- `deleteEvent(int eventId, int requestingUserId)` → `void`
- `toResponseWithSeats(Event)` → `EventResponseDTO` (privato)
- `checkOwnershipOrAdmin(Event, int)` → privato
- `resolveTags(List<Integer>)`, `resolveSpeakers(List<Integer>)` → privati

**TicketService**
- `bookTicket(TicketRequestDTO dto, int userId)` → `TicketResponseDTO`
- `cancelTicket(int ticketId, int userId)` → `void`
- `getUserTickets(int userId)` → `List<TicketResponseDTO>`
- `getEventParticipants(int eventId, int requestingUserId)` → `List<TicketResponseDTO>`

**FeedbackService**
- `leaveFeedback(FeedbackRequestDTO dto, int userId)` → `FeedbackResponseDTO`
- `getEventFeedbacks(int eventId)` → `List<FeedbackResponseDTO>`
- `getEventRating(int eventId)` → `Double`
- `deleteFeedback(int feedbackId)` → `void`

### EventService.getAllEvents — codice completo confermato

```java
public List<EventResponseDTO> getAllEvents(LocalDateTime date, Integer tagId, Integer venueId) {
    List<Event> events = new ArrayList<>();

    Tag tag = null;
    if (tagId != null) {
        tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag non trovato con id: " + tagId));
    }

    if (date != null && tag != null) {
        events = eventRepository.findByStartDateAfterAndTagsContaining(date, tag);
    } else if (date != null) {
        events = eventRepository.findByStartDateAfter(date);
    } else if (tag != null) {
        events = eventRepository.findByTagsContaining(tag);
    } else if (venueId != null) {
        events = eventRepository.findByVenueId(venueId);
    } else {
        events = eventRepository.findAll();
    }

    if (venueId != null && (date != null || tag != null)) {
        int vid = venueId;
        events = events.stream()
                .filter(e -> e.getVenue().getId() == vid)
                .toList();
    }

    return events.stream()
            .map(this::toResponseWithSeats)
            .toList();
}
```

---

## Controller (package `com.academy.eventhub.api`)

Regole:
- `@AuthenticationPrincipal CustomUserDetails userDetails` + `userDetails.getId()` per l'utente autenticato
- `@RequiredArgsConstructor` su ogni controller
- `@Operation`, `@ApiResponses`, `@Parameter` su ogni endpoint

| Controller | Endpoint gestiti |
|---|---|
| `AuthController` | `POST /auth/signup` |
| `UserController` | `GET /me`, `GET/PUT /admin/users/{id}/...` |
| `UserProfileController` | `GET/POST/PUT /me/profile` |
| `VenueController` | `GET /venues`, `GET /venues/{id}`, CRUD su `/admin/venues` |
| `TagController` | `GET /tags`, `POST/DELETE /admin/tags` |
| `SpeakerController` | `GET /speakers`, `GET /speakers/{id}`, CRUD su `/admin/speakers` |
| `EventController` | `GET /events` (con filtri), `GET /events/{id}`, `GET /events/my`, `POST/PUT/DELETE /events/{id}`, `GET /events/{id}/participants` |
| `TicketController` | `GET /tickets/my`, `POST /events/{eventId}/book`, `DELETE /tickets/{id}` |
| `FeedbackController` | `GET /events/{eventId}/feedbacks`, `GET /events/{eventId}/rating`, `POST /feedbacks`, `DELETE /admin/feedbacks/{id}` |

---

## Frontend

### js/auth.js — funzioni globali disponibili

```javascript
getAuthHeader()     // restituisce l'header Authorization da sessionStorage
getUser()           // restituisce l'oggetto user (id, email, role, status)
isLoggedIn()        // true se c'è un header salvato
logout()            // pulisce sessionStorage e redirect a login.html
apiFetch(url, opts) // fetch con Authorization header automatico, redirect a login se 401
renderNavbar(id)    // popola #navbar con link dinamici in base al ruolo
```

Chiavi sessionStorage: `eventhub_auth` (header Basic), `eventhub_user` (JSON utente)

### Pagine HTML

| File | Ruolo | Note |
|---|---|---|
| `login.html` | Tutti | Form login HTTP Basic, redirect per ruolo dopo login |
| `signup.html` | Tutti | POST /auth/signup |
| `events.html` | Tutti | Lista eventi con filtri data/tag |
| `event-detail.html` | Tutti | Dettaglio evento, form prenotazione, feedback |
| `my-bookings.html` | USER | Lista biglietti, cancellazione, form feedback |
| `profile.html` | USER | Visualizza/aggiorna profilo (POST se non esiste, PUT se esiste) |
| `organizer-events.html` | ORGANIZER, ADMIN | Lista eventi propri, form crea/modifica, partecipanti |
| `admin.html` | ADMIN | Tab: Utenti, Sedi, Relatori, Tag |

### CSS — `style.css`

Classi/ID rilevanti per future modifiche:
- `#navbar` — barra navigazione
- `#loginForm` — form centrato (body:has(#loginForm) centra la pagina)
- `#filters` — barra filtri eventi (flex row)
- `#eventsList`, `#myEventsList` — card eventi (div bianchi con border)
- `#eventForm` — form evento organizer (div nascosto, max-width 640px)
- `#participantsList` — pannello partecipanti (sfondo #eff6ff)
- `#tabs` + `.tab-content` — sistema tab admin
- `#usersList`, `#venuesList`, `#speakersList`, `#tagsList` — liste admin (flex row)
- `#venueForm`, `#speakerForm`, `#tagForm` — form inline admin (flex wrap, input senza div wrapper)
- `#venueMsg`, `#speakerMsg`, `#tagMsg`, `#eventFormMsg`, `#feedbackMsg` — messaggi feedback (colore impostato via JS)
- `button[onclick*="delete"]`, `button[onclick*="ban"]` — bottoni distruttivi (rosso)

---

## Test (package `com.academy.eventhub.service`)

### EventServiceTest — 12 test
- `getEventById`: trovato / non trovato
- `getAllEvents`: senza filtri / con filtro data
- `getEventsByOrganizer`: happy path
- `createEvent`: organizer attivo / organizer bannato / venue non trovata
- `updateEvent`: owner / admin / non autorizzato
- `deleteEvent`: owner / non autorizzato

### TicketServiceTest — 13 test
- `bookTicket`: happy path / utente bannato / evento passato / prenotazione proprio evento / doppia prenotazione / posti esauriti
- `cancelTicket`: happy path / biglietto altrui / già cancellato / evento passato
- `getUserTickets`: happy path
- `getEventParticipants`: owner / admin / non autorizzato

---

## TODO post-consegna

- [ ] Migrazione da MySQL a PostgreSQL
- [ ] Docker Compose con PostgreSQL + Adminer
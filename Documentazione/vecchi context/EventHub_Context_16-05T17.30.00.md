# EventHub — Context Dump
> File di contesto del progetto. Da reinserire ad ogni nuova sessione.
> Ultimo aggiornamento: 16/05/2026

---

## Note operative per Claude

- Scrivere sempre il codice **direttamente in chat** (inline), non come file da scaricare.
- **Non inventare mai** nomi di classi, metodi, campi senza conferma esplicita. Se qualcosa non è documentato qui, chiedere prima di procedere.
- Il file `EventHub_Academy_2026.md` è LEGGE — qualsiasi deviazione va approvata esplicitamente dall'utente.

---

## Panoramica del progetto

Piattaforma di gestione eventi con prenotazione biglietti e feedback.
Progetto accademico con roadmap da 15 giorni.

**Deliverable finali:** repo Git + Swagger UI + demo live

---

## Regole fondamentali del progetto

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
- ADMIN può promuovere USER → ORGANIZER (non ORGANIZER → ADMIN)

### Status utente — enum interno a User
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
- Separati in Request (validazione) e Response (`@Schema`)
- `@Schema` solo sui DTO Response, mai sulle entity

### Prezzi
- `BigDecimal` per i prezzi (evita errori floating point)

### Dependency injection
- Si usa `@RequiredArgsConstructor` di Lombok su tutte le classi (genera constructor injection per i campi `final`). Non si usa `@Autowired`.

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

> Nota: `@AssertTrue isEndDateAfterStartDate()` non è sull'entity — la validazione cross-field è su `EventRequestDTO`.

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
| `EventRequestDTO` | Request | tutti i campi + venueId, tagIds, speakerIds + `@AssertTrue` date |
| `EventResponseDTO` | Response | tutti i campi + availableSeats (calcolato) + oggetti annidati |
| `TicketRequestDTO` | Request | type, eventId — ha setter su eventId (usato dal controller) |
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
- solo CRUD base (estende JpaRepository)

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

### AuthService — codice completo confermato
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.SignupRequestDTO;
import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.entity.User;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.mapper.UserMapper;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO signup(SignupRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email già in uso: " + dto.getEmail());
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);

        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
```

### UserService — codice completo confermato
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.entity.User;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.UserMapper;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    public UserResponseDTO getUserById(int id) {
        return userMapper.toResponseDTO(findUserOrThrow(id));
    }

    public UserResponseDTO promoteUser(int id) {
        User user = findUserOrThrow(id);
        if (user.getRole() != User.Role.USER) {
            throw new BusinessException("Solo un utente con ruolo USER può essere promosso a ORGANIZER");
        }
        user.setRole(User.Role.ORGANIZER);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO banUser(int id) {
        User user = findUserOrThrow(id);
        if (user.getRole() == User.Role.ADMIN) {
            throw new BusinessException("Non puoi bannare un amministratore");
        }
        if (user.getStatus() == User.Status.BANNED) {
            throw new BusinessException("L'utente è già bannato");
        }
        user.setStatus(User.Status.BANNED);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    public UserResponseDTO reactivateUser(int id) {
        User user = findUserOrThrow(id);
        if (user.getStatus() == User.Status.ACTIVE) {
            throw new BusinessException("L'utente è già attivo");
        }
        user.setStatus(User.Status.ACTIVE);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    private User findUserOrThrow(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + id));
    }
}
```

### VenueService — codice completo confermato
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.entity.Venue;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.VenueMapper;
import com.academy.eventhub.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;

    public List<VenueResponseDTO> getAllVenues() {
        return venueRepository.findAll()
                .stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    public VenueResponseDTO getVenueById(int id) {
        return venueMapper.toResponseDTO(findVenueOrThrow(id));
    }

    public VenueResponseDTO createVenue(VenueRequestDTO dto) {
        if (venueRepository.existsByName(dto.getName())) {
            throw new BusinessException("Esiste già una sede con il nome: " + dto.getName());
        }
        return venueMapper.toResponseDTO(venueRepository.save(venueMapper.toEntity(dto)));
    }

    public VenueResponseDTO updateVenue(int id, VenueRequestDTO dto) {
        Venue venue = findVenueOrThrow(id);
        venueMapper.updateEntity(dto, venue);
        return venueMapper.toResponseDTO(venueRepository.save(venue));
    }

    public void deleteVenue(int id) {
        findVenueOrThrow(id);
        venueRepository.deleteById(id);
    }

    private Venue findVenueOrThrow(int id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede non trovata con id: " + id));
    }
}
```

### TagService — codice completo confermato
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.TagRequestDTO;
import com.academy.eventhub.dto.TagResponseDTO;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.TagMapper;
import com.academy.eventhub.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toResponseDTO)
                .toList();
    }

    public TagResponseDTO createTag(TagRequestDTO dto) {
        if (tagRepository.existsByName(dto.getName())) {
            throw new BusinessException("Esiste già un tag con il nome: " + dto.getName());
        }
        return tagMapper.toResponseDTO(tagRepository.save(tagMapper.toEntity(dto)));
    }

    public void deleteTag(int id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag non trovato con id: " + id);
        }
        tagRepository.deleteById(id);
    }
}
```

### SpeakerService — codice completo confermato
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.entity.Speaker;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.SpeakerMapper;
import com.academy.eventhub.repository.SpeakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final SpeakerRepository speakerRepository;
    private final SpeakerMapper speakerMapper;

    public List<SpeakerResponseDTO> getAllSpeakers() {
        return speakerRepository.findAll()
                .stream()
                .map(speakerMapper::toResponseDTO)
                .toList();
    }

    public SpeakerResponseDTO getSpeakerById(int id) {
        return speakerMapper.toResponseDTO(findSpeakerOrThrow(id));
    }

    public SpeakerResponseDTO createSpeaker(SpeakerRequestDTO dto) {
        return speakerMapper.toResponseDTO(speakerRepository.save(speakerMapper.toEntity(dto)));
    }

    public SpeakerResponseDTO updateSpeaker(int id, SpeakerRequestDTO dto) {
        Speaker speaker = findSpeakerOrThrow(id);
        speakerMapper.updateEntity(dto, speaker);
        return speakerMapper.toResponseDTO(speakerRepository.save(speaker));
    }

    public void deleteSpeaker(int id) {
        findSpeakerOrThrow(id);
        speakerRepository.deleteById(id);
    }

    private Speaker findSpeakerOrThrow(int id) {
        return speakerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatore non trovato con id: " + id));
    }
}
```

### UserProfileService — firme confermate
- `getProfileByUserId(int userId)` → `UserProfileResponseDTO`
- `createProfile(int userId, UserProfileRequestDTO dto)` → `UserProfileResponseDTO` — lancia `BusinessException` se profilo già esistente
- `updateProfile(int userId, UserProfileRequestDTO dto)` → `UserProfileResponseDTO`

### EventService — firme confermate + getAllEvents aggiornato

`getAllEvents` è stato aggiornato per supportare filtri opzionali. **Il metodo originale `getAllEvents()` va sostituito con questo:**

```java
// Dipendenze necessarie in EventService (aggiungere TagRepository se non già presente):
// private final TagRepository tagRepository;

public List<EventResponseDTO> getAllEvents(LocalDateTime date, Integer tagId, Integer venueId) {
    List<Event> events;

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

    // venueId combinato con altri filtri: nessun metodo repo per tutte le combinazioni, si applica in stream
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

Altre firme EventService:
- `getEventById(int id)` → `EventResponseDTO`
- `getEventsByOrganizer(int organizerId)` → `List<EventResponseDTO>`
- `createEvent(EventRequestDTO dto, int organizerId)` → `EventResponseDTO`
- `updateEvent(int eventId, EventRequestDTO dto, int requestingUserId)` → `EventResponseDTO`
- `deleteEvent(int eventId, int requestingUserId)` → `void`
- `toResponseWithSeats(Event)` → `EventResponseDTO` (privato, calcola availableSeats)
- `checkOwnershipOrAdmin(Event, int)` → privato
- `resolveTags(List<Integer>)` e `resolveSpeakers(List<Integer>)` → privati

### TicketService — firme confermate
- `bookTicket(TicketRequestDTO dto, int userId)` → `TicketResponseDTO`
- `cancelTicket(int ticketId, int userId)` → `void`
- `getUserTickets(int userId)` → `List<TicketResponseDTO>`
- `getEventParticipants(int eventId, int requestingUserId)` → `List<TicketResponseDTO>`

### FeedbackService — firme confermate
- `leaveFeedback(FeedbackRequestDTO dto, int userId)` → `FeedbackResponseDTO`
- `getEventFeedbacks(int eventId)` → `List<FeedbackResponseDTO>`
- `getEventRating(int eventId)` → `Double`
- `deleteFeedback(int feedbackId)` → `void`

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

**Tipo:** HTTP Basic + BCrypt

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
- `DaoAuthenticationProvider` + `BCryptPasswordEncoder`

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
| `GET /events/my`, `GET /events/{id}/participants` | ORGANIZER o ADMIN |
| `/me/**` | autenticato |
| `POST /events/{eventId}/book`, `/tickets/**`, `/feedbacks/**` | autenticato |

---

## Controller (package `com.academy.eventhub.api`)

Regole generali:
- `@AuthenticationPrincipal CustomUserDetails userDetails` per ricavare l'utente autenticato
- `userDetails.getId()` per ricavare l'ID
- `@RequiredArgsConstructor` su ogni controller
- Documentazione Swagger: `@Operation`, `@ApiResponses`, `@Parameter` su ogni endpoint

### AuthController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.SignupRequestDTO;
import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registrazione alla piattaforma")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(
            summary = "Registra un nuovo utente",
            description = "Crea un account con ruolo USER e status ACTIVE. Non richiede autenticazione."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utente registrato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Email già in uso")
    })
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody SignupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(dto));
    }
}
```

### UserController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Area personale e gestione utenti (ADMIN)")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Dati utente corrente", description = "Restituisce email, ruolo e status dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dati restituiti"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Lista tutti gli utenti", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista utenti"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/users/{id}")
    @Operation(summary = "Dettaglio utente per ID", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente trovato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID utente") @PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/admin/users/{id}/promote")
    @Operation(
            summary = "Promuove un utente a ORGANIZER",
            description = "Solo ADMIN. Applicabile solo a utenti con ruolo USER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ruolo aggiornato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Utente non è USER")
    })
    public ResponseEntity<UserResponseDTO> promoteUser(
            @Parameter(description = "ID utente da promuovere") @PathVariable int id) {
        return ResponseEntity.ok(userService.promoteUser(id));
    }

    @PutMapping("/admin/users/{id}/ban")
    @Operation(
            summary = "Banna un utente",
            description = "Solo ADMIN. Non applicabile agli ADMIN. Cancella automaticamente i biglietti futuri."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente bannato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Target è ADMIN o già bannato")
    })
    public ResponseEntity<UserResponseDTO> banUser(
            @Parameter(description = "ID utente da bannare") @PathVariable int id) {
        return ResponseEntity.ok(userService.banUser(id));
    }

    @PutMapping("/admin/users/{id}/reactivate")
    @Operation(summary = "Riattiva un utente bannato", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente riattivato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Utente già attivo")
    })
    public ResponseEntity<UserResponseDTO> reactivateUser(
            @Parameter(description = "ID utente da riattivare") @PathVariable int id) {
        return ResponseEntity.ok(userService.reactivateUser(id));
    }
}
```

### UserProfileController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Gestione profilo anagrafico dell'utente autenticato")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "Leggi il tuo profilo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo trovato"),
            @ApiResponse(responseCode = "404", description = "Profilo non ancora creato"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Crea il tuo profilo", description = "Può essere fatto una sola volta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profilo creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Profilo già esistente"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> createMyProfile(
            @Valid @RequestBody UserProfileRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.createProfile(userDetails.getId(), dto));
    }

    @PutMapping
    @Operation(summary = "Aggiorna il tuo profilo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Profilo non trovato"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(
            @Valid @RequestBody UserProfileRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.updateProfile(userDetails.getId(), dto));
    }
}
```

### VenueController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Catalogo sedi (lettura pubblica, scrittura solo ADMIN)")
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/venues")
    @Operation(summary = "Lista tutte le sedi")
    @ApiResponse(responseCode = "200", description = "Lista sedi")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/venues/{id}")
    @Operation(summary = "Dettaglio sede per ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede trovata"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<VenueResponseDTO> getVenueById(
            @Parameter(description = "ID sede") @PathVariable int id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @PostMapping("/admin/venues")
    @Operation(summary = "Crea una nuova sede", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sede creata"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Sede con lo stesso nome già esistente")
    })
    public ResponseEntity<VenueResponseDTO> createVenue(@Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(dto));
    }

    @PutMapping("/admin/venues/{id}")
    @Operation(summary = "Modifica una sede esistente", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede aggiornata"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @Parameter(description = "ID sede") @PathVariable int id,
            @Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.ok(venueService.updateVenue(id, dto));
    }

    @DeleteMapping("/admin/venues/{id}")
    @Operation(summary = "Elimina una sede", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sede eliminata"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<Void> deleteVenue(
            @Parameter(description = "ID sede") @PathVariable int id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
```

### TagController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.TagRequestDTO;
import com.academy.eventhub.dto.TagResponseDTO;
import com.academy.eventhub.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Catalogo categorie (lettura pubblica, scrittura solo ADMIN)")
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    @Operation(summary = "Lista tutti i tag")
    @ApiResponse(responseCode = "200", description = "Lista tag")
    public ResponseEntity<List<TagResponseDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping("/admin/tags")
    @Operation(summary = "Crea un nuovo tag", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Tag con lo stesso nome già esistente")
    })
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody TagRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(dto));
    }

    @DeleteMapping("/admin/tags/{id}")
    @Operation(summary = "Elimina un tag", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag eliminato"),
            @ApiResponse(responseCode = "404", description = "Tag non trovato")
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID tag") @PathVariable int id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
```

### SpeakerController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.service.SpeakerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Speakers", description = "Catalogo relatori (lettura pubblica, scrittura solo ADMIN)")
public class SpeakerController {

    private final SpeakerService speakerService;

    @GetMapping("/speakers")
    @Operation(summary = "Lista tutti i relatori")
    @ApiResponse(responseCode = "200", description = "Lista relatori")
    public ResponseEntity<List<SpeakerResponseDTO>> getAllSpeakers() {
        return ResponseEntity.ok(speakerService.getAllSpeakers());
    }

    @GetMapping("/speakers/{id}")
    @Operation(summary = "Dettaglio relatore per ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatore trovato"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<SpeakerResponseDTO> getSpeakerById(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        return ResponseEntity.ok(speakerService.getSpeakerById(id));
    }

    @PostMapping("/admin/speakers")
    @Operation(summary = "Crea un nuovo relatore", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Relatore creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    public ResponseEntity<SpeakerResponseDTO> createSpeaker(@Valid @RequestBody SpeakerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.createSpeaker(dto));
    }

    @PutMapping("/admin/speakers/{id}")
    @Operation(summary = "Modifica un relatore esistente", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatore aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<SpeakerResponseDTO> updateSpeaker(
            @Parameter(description = "ID relatore") @PathVariable int id,
            @Valid @RequestBody SpeakerRequestDTO dto) {
        return ResponseEntity.ok(speakerService.updateSpeaker(id, dto));
    }

    @DeleteMapping("/admin/speakers/{id}")
    @Operation(summary = "Elimina un relatore", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Relatore eliminato"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<Void> deleteSpeaker(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        speakerService.deleteSpeaker(id);
        return ResponseEntity.noContent().build();
    }
}
```

### EventController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.EventService;
import com.academy.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Catalogo eventi, CRUD organizer e lista partecipanti")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    @GetMapping
    @Operation(
            summary = "Lista eventi pubblici",
            description = "Filtri opzionali combinabili: date (ISO-8601), tagId, venueId. Es: /events?date=2025-06-01T00:00:00&tagId=3"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista eventi"),
            @ApiResponse(responseCode = "400", description = "Formato data non valido")
    })
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @Parameter(description = "Filtra eventi con startDate > questa data")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,

            @Parameter(description = "Filtra per ID tag")
            @RequestParam(required = false) Integer tagId,

            @Parameter(description = "Filtra per ID sede")
            @RequestParam(required = false) Integer venueId) {
        return ResponseEntity.ok(eventService.getAllEvents(date, tagId, venueId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio evento", description = "Include sede, organizer, relatori, tag, posti disponibili.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento trovato"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID evento") @PathVariable int id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "I miei eventi", description = "Lista degli eventi creati dall'organizer autenticato. ORGANIZER o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista eventi dell'organizer"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<List<EventResponseDTO>> getMyEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Crea un nuovo evento", description = "ORGANIZER o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi o date incoerenti"),
            @ApiResponse(responseCode = "404", description = "Venue, tag o speaker non trovati"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(dto, userDetails.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica un evento", description = "ORGANIZER (solo i propri) o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "ID evento") @PathVariable int id,
            @Valid @RequestBody EventRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un evento", description = "ORGANIZER (solo i propri) o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminato"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.deleteEvent(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Lista partecipanti", description = "ORGANIZER (solo i propri eventi) o ADMIN. Restituisce i biglietti attivi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista biglietti attivi"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<List<TicketResponseDTO>> getEventParticipants(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getEventParticipants(id, userDetails.getId()));
    }
}
```

### TicketController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.TicketRequestDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Prenotazione e cancellazione biglietti")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/tickets/my")
    @Operation(summary = "Le mie prenotazioni", description = "Lista di tutti i biglietti dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista biglietti"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getUserTickets(userDetails.getId()));
    }

    @PostMapping("/events/{eventId}/book")
    @Operation(
            summary = "Prenota un biglietto",
            description = "Prenota STANDARD o VIP. Il prezzo è impostato automaticamente dal service. Vincoli: evento futuro, nessuna doppia prenotazione, posti disponibili, utente non bannato."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Biglietto prenotato"),
            @ApiResponse(responseCode = "400", description = "Tipo biglietto non valido"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "409", description = "Evento passato, doppia prenotazione, posti esauriti o utente bannato")
    })
    public ResponseEntity<TicketResponseDTO> bookTicket(
            @Parameter(description = "ID evento") @PathVariable int eventId,
            @Valid @RequestBody TicketRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        dto.setEventId(eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.bookTicket(dto, userDetails.getId()));
    }

    @DeleteMapping("/tickets/{id}")
    @Operation(
            summary = "Cancella una prenotazione",
            description = "Consentita solo prima dell'inizio dell'evento. Il biglietto viene marcato CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Prenotazione cancellata"),
            @ApiResponse(responseCode = "403", description = "Il biglietto non appartiene all'utente"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "409", description = "Biglietto già cancellato o evento già iniziato")
    })
    public ResponseEntity<Void> cancelTicket(
            @Parameter(description = "ID biglietto") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketService.cancelTicket(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
```

### FeedbackController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.FeedbackRequestDTO;
import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Feedbacks", description = "Recensioni post-evento")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/events/{eventId}/feedbacks")
    @Operation(summary = "Feedback di un evento", description = "Pubblica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista feedback"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<List<FeedbackResponseDTO>> getEventFeedbacks(
            @Parameter(description = "ID evento") @PathVariable int eventId) {
        return ResponseEntity.ok(feedbackService.getEventFeedbacks(eventId));
    }

    @GetMapping("/events/{eventId}/rating")
    @Operation(summary = "Valutazione media di un evento", description = "Pubblica. Null se nessun feedback presente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media calcolata"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<Double> getEventRating(
            @Parameter(description = "ID evento") @PathVariable int eventId) {
        return ResponseEntity.ok(feedbackService.getEventRating(eventId));
    }

    @PostMapping("/feedbacks")
    @Operation(
            summary = "Lascia un feedback",
            description = "Vincoli: evento concluso, biglietto attivo presente, nessun feedback duplicato."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feedback registrato"),
            @ApiResponse(responseCode = "400", description = "Voto non nel range 1-5"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "409", description = "Evento non concluso, nessun biglietto valido o feedback già presente")
    })
    public ResponseEntity<FeedbackResponseDTO> leaveFeedback(
            @Valid @RequestBody FeedbackRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.leaveFeedback(dto, userDetails.getId()));
    }

    @DeleteMapping("/admin/feedbacks/{id}")
    @Operation(summary = "Elimina un feedback", description = "Solo ADMIN. Moderazione contenuti.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Feedback eliminato"),
            @ApiResponse(responseCode = "404", description = "Feedback non trovato"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<Void> deleteFeedback(
            @Parameter(description = "ID feedback") @PathVariable int id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## TODO aggiornato

- [x] Entity complete
- [x] DTO completi
- [x] Mapper MapStruct
- [x] Repository
- [x] Service (AuthService, UserService, VenueService, TagService, SpeakerService — codice completo confermato; altri — firme confermate)
- [x] Eccezioni custom + GlobalExceptionHandler
- [x] Security (CustomUserDetails, CustomUserDetailsService, SecurityConfig)
- [x] Controller REST (tutti e 9, con Swagger)
- [x] Filtri GET /events (date, tagId, venueId) — aggiornare EventService.getAllEvents()
- [ ] **Test JUnit** (EventService, TicketService) ← prossimo step
- [ ] Frontend HTML/CSS/JS vanilla
- [ ] Abilitare Swagger (`enabled=true` in application.properties)
- [ ] README completo
- [ ] Migrazione PostgreSQL + Docker Compose (quando richiesto)

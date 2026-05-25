# EventHub — Context Dump (unificato)
> File di contesto del progetto. Da reinserire ad ogni nuova sessione.
> Ultimo aggiornamento: 24/05/2026 — merge dei context del 17/05 e 24/05

---

## Note operative per Claude

- Scrivere sempre il codice **direttamente in chat** (inline), non come file da scaricare.
- **Non inventare mai** nomi di classi, metodi, campi senza conferma esplicita. Se qualcosa non è documentato qui, chiedere prima di procedere.
- Il file `EventHub_Academy_2026.md` è LEGGE — qualsiasi deviazione va approvata esplicitamente dall'utente.
- Le pagine HTML non sono documentate nel dettaglio nel context — se servono modifiche, chiedere il file prima di procedere.
- Chiedere sempre conferma prima di usare classi/metodi non documentati qui.

---

## Stato del progetto

**Progetto completato e consegnato.**
Tutto il backend, frontend, test e README sono stati implementati.

Attività future (post-consegna):
- [ ] Migrazione da MySQL a PostgreSQL
- [ ] Docker Compose con PostgreSQL + Adminer

---

## Repository

https://github.com/mirto97/EventHub--LargeSystemsAcademy.git

---

## Regole fondamentali

- Il file `EventHub_Academy_2026.md` è LEGGE
- MySQL al posto di PostgreSQL: **concesso**
- MapStruct: **concesso**
- HTTP Basic + BCrypt: **requisito rispettato**
- Niente framework frontend (no React/Vue/Angular): **requisito rispettato**

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

Frontend:
```
src/main/resources/static/
├── css/style.css
├── html/             ← login, signup, events, event-detail,
│                        my-bookings, profile, organizer-events, admin
└── js/auth.js
```

Test:
```
src/test/java/com/academy/eventhub/service/
├── EventServiceTest.java
└── TicketServiceTest.java
```

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

- **Ruoli:** enum interno a `User` — `USER`, `ORGANIZER`, `ADMIN` (non gerarchici). ADMIN può promuovere USER → ORGANIZER (non ORGANIZER → ADMIN).
- **Status utente:** enum interno a `User` — `ACTIVE`, `BANNED`
- **Ticket status:** enum separato — `ACTIVE`, `CANCELLED` (tenuti separati perché semanticamente diversi: BANNED ≠ CANCELLED)
- **UserProfile:** composizione, non ereditarietà — `@OneToOne` con `User`; UserProfile ha la FK `user_id`
- **Lombok su @Entity:** Mai `@Data` sulle entity JPA (rischio LazyInitializationException). Usare `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`
- **DTO:** `@Data @NoArgsConstructor @AllArgsConstructor` sui DTO è OK. Separati in Request (validazione) e Response (`@Schema`). `@Schema` solo sui DTO Response, mai sulle entity.
- **Prezzi:** `BigDecimal` per i prezzi (evita errori floating point)
- **Dependency injection:** `@RequiredArgsConstructor` su tutte le classi. Mai `@Autowired`.
- **Security:** HTTP Basic con `Customizer.withDefaults()`, stateless, CSRF disabilitato. `DaoAuthenticationProvider(userDetailsService)` + `setPasswordEncoder()` — nota: in Spring Security 7 il costruttore vuole UserDetailsService, non no-args.
- **Validazione cross-field:** `@AssertTrue isEndDateAfterStartDate()` su `EventRequestDTO`, non sull'entity.

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

Tutti flat nella stessa cartella. `@Data @NoArgsConstructor @AllArgsConstructor` su tutti.
`@Schema` solo sui Response. Validazione solo sui Request.

### SignupRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SignupRequestDTO {

    @Email(message = "Email non valida")
    @NotBlank(message = "L'email è obbligatoria")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8, message = "La password deve essere di almeno 8 caratteri")
    private String password;
}
```

### UserResponseDTO.java
```java
package com.academy.eventhub.dto;

import com.academy.eventhub.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati pubblici di un utente")
public class UserResponseDTO {

    @Schema(description = "ID utente", example = "1")
    private int id;

    @Schema(description = "Email utente", example = "mario@example.com")
    private String email;

    @Schema(description = "Ruolo utente", example = "USER")
    private User.Role role;

    @Schema(description = "Stato utente", example = "ACTIVE")
    private User.Status status;
}
```

### UserProfileRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UserProfileRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;

    private String bio;
    private String city;
    private String photoUrl;
}
```

### UserProfileResponseDTO.java
```java
package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati del profilo utente")
public class UserProfileResponseDTO {

    @Schema(description = "ID profilo", example = "1")
    private int id;

    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia")
    private String bio;

    @Schema(description = "Città", example = "Roma")
    private String city;

    @Schema(description = "URL foto profilo")
    private String photoUrl;
}
```

### VenueRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class VenueRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    private String address;

    @NotNull(message = "La capienza è obbligatoria")
    @Min(value = 1, message = "La capienza deve essere almeno 1")
    private Integer capacity;
}
```

### VenueResponseDTO.java
```java
package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di una sede")
public class VenueResponseDTO {

    @Schema(description = "ID sede", example = "1")
    private int id;

    @Schema(description = "Nome della sede", example = "Centro Congressi Roma")
    private String name;

    @Schema(description = "Indirizzo", example = "Via Roma 1, Roma")
    private String address;

    @Schema(description = "Capienza massima", example = "200")
    private int capacity;
}
```

### TagRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TagRequestDTO {

    @NotBlank(message = "Il nome del tag è obbligatorio")
    private String name;
}
```

### TagResponseDTO.java
```java
package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un tag")
public class TagResponseDTO {

    @Schema(description = "ID tag", example = "1")
    private int id;

    @Schema(description = "Nome del tag", example = "Java")
    private String name;
}
```

### SpeakerRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SpeakerRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String lastName;

    private String bio;
    private String photoUrl;
}
```

### SpeakerResponseDTO.java
```java
package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un relatore")
public class SpeakerResponseDTO {

    @Schema(description = "ID relatore", example = "1")
    private int id;

    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia")
    private String bio;

    @Schema(description = "URL foto")
    private String photoUrl;
}
```

### EventRequestDTO.java
```java
package com.academy.eventhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class EventRequestDTO {

    @NotBlank(message = "Il titolo è obbligatorio")
    private String title;

    private String description;

    @NotNull(message = "La data di inizio è obbligatoria")
    private LocalDateTime startDate;

    @NotNull(message = "La data di fine è obbligatoria")
    private LocalDateTime endDate;

    @NotNull(message = "Il prezzo standard è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    private BigDecimal standardPrice;

    @NotNull(message = "Il prezzo VIP è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    private BigDecimal vipPrice;

    @NotNull(message = "La sede è obbligatoria")
    private Integer venueId;

    private List<Integer> tagIds;
    private List<Integer> speakerIds;

    @AssertTrue(message = "La data di fine deve essere successiva alla data di inizio")
    @Schema(hidden = true)
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }
}
```

### EventResponseDTO.java
```java
package com.academy.eventhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un evento")
public class EventResponseDTO {

    @Schema(description = "ID evento", example = "1")
    private int id;

    @Schema(description = "Titolo")
    private String title;

    @Schema(description = "Descrizione")
    private String description;

    @Schema(description = "Data e ora di inizio")
    private LocalDateTime startDate;

    @Schema(description = "Data e ora di fine")
    private LocalDateTime endDate;

    @Schema(description = "Prezzo standard", example = "49.99")
    private BigDecimal standardPrice;

    @Schema(description = "Prezzo VIP", example = "99.99")
    private BigDecimal vipPrice;

    @Schema(description = "Posti ancora disponibili", example = "42")
    private int availableSeats;

    @Schema(description = "Sede dell'evento")
    private VenueResponseDTO venue;

    @Schema(description = "Organizzatore dell'evento")
    private UserResponseDTO organizer;

    @Schema(description = "Tag dell'evento")
    private List<TagResponseDTO> tags;

    @Schema(description = "Relatori dell'evento")
    private List<SpeakerResponseDTO> speakers;
}
```

### TicketRequestDTO.java
```java
package com.academy.eventhub.dto;

import com.academy.eventhub.entity.Ticket;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TicketRequestDTO {

    @NotNull(message = "Il tipo di biglietto è obbligatorio")
    private Ticket.TicketType type;

    @NotNull(message = "L'evento è obbligatorio")
    private Integer eventId;
}
```

> **Nota:** `eventId` ha un setter usato dal controller (`dto.setEventId(eventId)`) per impostare l'ID dall'URL prima di passare il DTO al service.

### TicketResponseDTO.java
```java
package com.academy.eventhub.dto;

import com.academy.eventhub.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un biglietto prenotato")
public class TicketResponseDTO {

    @Schema(description = "ID biglietto", example = "1")
    private int id;

    @Schema(description = "Tipo di biglietto", example = "STANDARD")
    private Ticket.TicketType type;

    @Schema(description = "Prezzo pagato", example = "49.99")
    private BigDecimal price;

    @Schema(description = "Stato del biglietto", example = "ACTIVE")
    private Ticket.TicketStatus status;

    @Schema(description = "ID evento", example = "1")
    private int eventId;

    @Schema(description = "Titolo evento")
    private String eventTitle;
}
```

### FeedbackRequestDTO.java
```java
package com.academy.eventhub.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class FeedbackRequestDTO {

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private Integer rating;

    private String comment;

    @NotNull(message = "L'evento è obbligatorio")
    private Integer eventId;
}
```

### FeedbackResponseDTO.java
```java
package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un feedback")
public class FeedbackResponseDTO {

    @Schema(description = "ID feedback", example = "1")
    private int id;

    @Schema(description = "Voto", example = "4")
    private int rating;

    @Schema(description = "Commento")
    private String comment;

    @Schema(description = "Utente che ha lasciato il feedback")
    private UserResponseDTO user;

    @Schema(description = "ID evento recensito", example = "1")
    private int eventId;

    @Schema(description = "Titolo evento recensito")
    private String eventTitle;
}
```

---

## Mapper (package `com.academy.eventhub.mapper`)

### UserMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}
```

### UserProfileMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileResponseDTO toResponseDTO(UserProfile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserProfile toEntity(UserProfileRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(UserProfileRequestDTO dto, @MappingTarget UserProfile profile);
}
```

### VenueMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VenueMapper {
    VenueResponseDTO toResponseDTO(Venue venue);

    @Mapping(target = "id", ignore = true)
    Venue toEntity(VenueRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(VenueRequestDTO dto, @MappingTarget Venue venue);
}
```

### TagMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.TagRequestDTO;
import com.academy.eventhub.dto.TagResponseDTO;
import com.academy.eventhub.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponseDTO toResponseDTO(Tag tag);

    @Mapping(target = "id", ignore = true)
    Tag toEntity(TagRequestDTO dto);
}
```

### SpeakerMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.entity.Speaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SpeakerMapper {
    SpeakerResponseDTO toResponseDTO(Speaker speaker);

    @Mapping(target = "id", ignore = true)
    Speaker toEntity(SpeakerRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(SpeakerRequestDTO dto, @MappingTarget Speaker speaker);
}
```

### EventMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {VenueMapper.class, UserMapper.class, TagMapper.class, SpeakerMapper.class})
public interface EventMapper {

    @Mapping(target = "availableSeats", ignore = true)
    EventResponseDTO toResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "speakers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "feedbacks", ignore = true)
    Event toEntity(EventRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "speakers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "feedbacks", ignore = true)
    void updateEntity(EventRequestDTO dto, @MappingTarget Event event);
}
```

### TicketMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    TicketResponseDTO toResponseDTO(Ticket ticket);
}
```

### FeedbackMapper.java
```java
package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FeedbackMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    FeedbackResponseDTO toResponseDTO(Feedback feedback);
}
```

---

## Repository (package `com.academy.eventhub.repository`)

### UserRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### UserProfileRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByUserId(int userId);
}
```

### VenueRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Integer> {
    boolean existsByName(String name);
}
```

### TagRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
}
```

### SpeakerRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Speaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
```

### EventRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Event;
import com.academy.eventhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByOrganizerId(int organizerId);
    List<Event> findByStartDateAfter(LocalDateTime date);
    List<Event> findByVenueId(int venueId);
    List<Event> findByTagsContaining(Tag tag);
    List<Event> findByStartDateAfterAndTagsContaining(LocalDateTime date, Tag tag);
}
```

### TicketRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByUserId(int userId);
    List<Ticket> findByEventId(int eventId);
    boolean existsByUserIdAndEventId(int userId, int eventId);
    int countByEventIdAndStatus(int eventId, Ticket.TicketStatus status);
    Optional<Ticket> findByUserIdAndEventIdAndStatus(int userId, int eventId, Ticket.TicketStatus status);
}
```

### FeedbackRepository.java
```java
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByEventId(int eventId);
    List<Feedback> findByUserId(int userId);
    boolean existsByUserIdAndEventId(int userId, int eventId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") int eventId);
}
```

---

## Eccezioni (package `com.academy.eventhub.exception`)

### ResourceNotFoundException.java
```java
package com.academy.eventhub.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### BusinessException.java
```java
package com.academy.eventhub.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler.java
```java
package com.academy.eventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Errore di validazione");
        body.put("details", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno del server");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
```

Mapping risposte:
- `ResourceNotFoundException` → 404
- `BusinessException` → 409
- `MethodArgumentNotValidException` → 400 con mappa `field → message`
- `Exception` generica → 500
- Tutti i body hanno: `timestamp`, `status`, `error`

---

## Security (package `com.academy.eventhub.security`)

### CustomUserDetails.java
```java
package com.academy.eventhub.security;

import com.academy.eventhub.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final int id;
    private final String email;
    private final String password;
    private final User.Role role;
    private final User.Status status;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != User.Status.BANNED;
    }

    @Override
    public boolean isEnabled() {
        return status == User.Status.ACTIVE;
    }
}
```

### CustomUserDetailsService.java
```java
package com.academy.eventhub.security;

import com.academy.eventhub.entity.User;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));
        return new CustomUserDetails(user);
    }
}
```

### SecurityConfig.java
```java
package com.academy.eventhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                .requestMatchers(HttpMethod.GET, "/events", "/events/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/{id}/feedbacks").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/{id}/rating").permitAll()
                .requestMatchers(HttpMethod.GET, "/venues", "/venues/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/speakers", "/speakers/{id}").permitAll()
                .requestMatchers("/swagger-ui/**", "/apidocs/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/events").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/events/{id}").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/events/{id}").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/events/my").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/events/{id}/participants").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/me/**").authenticated()
                .requestMatchers("/events/{eventId}/book").authenticated()
                .requestMatchers("/tickets/**").authenticated()
                .requestMatchers("/feedbacks/**").authenticated()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Service (package `com.academy.eventhub.service`)

### AuthService.java
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

### UserService.java
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
        return userRepository.findAll().stream().map(userMapper::toResponseDTO).toList();
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

### UserProfileService.java
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.entity.User;
import com.academy.eventhub.entity.UserProfile;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.UserProfileMapper;
import com.academy.eventhub.repository.UserProfileRepository;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileResponseDTO getProfileByUserId(int userId) {
        return userProfileMapper.toResponseDTO(findProfileOrThrow(userId));
    }

    public UserProfileResponseDTO createProfile(int userId, UserProfileRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));
        if (userProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException("Il profilo per questo utente esiste già");
        }
        UserProfile profile = userProfileMapper.toEntity(dto);
        profile.setUser(user);
        return userProfileMapper.toResponseDTO(userProfileRepository.save(profile));
    }

    public UserProfileResponseDTO updateProfile(int userId, UserProfileRequestDTO dto) {
        UserProfile profile = findProfileOrThrow(userId);
        userProfileMapper.updateEntity(dto, profile);
        return userProfileMapper.toResponseDTO(userProfileRepository.save(profile));
    }

    private UserProfile findProfileOrThrow(int userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato per l'utente con id: " + userId));
    }
}
```

### VenueService.java
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
        return venueRepository.findAll().stream().map(venueMapper::toResponseDTO).toList();
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

### TagService.java
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
        return tagRepository.findAll().stream().map(tagMapper::toResponseDTO).toList();
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

### SpeakerService.java
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
        return speakerRepository.findAll().stream().map(speakerMapper::toResponseDTO).toList();
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

### EventService.java

> **Nota:** La firma di `getAllEvents` è stata aggiornata per supportare i filtri (`date`, `tagId`, `venueId`). Il metodo completo è riportato sotto.

```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.entity.*;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.EventMapper;
import com.academy.eventhub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final SpeakerRepository speakerRepository;
    private final TicketRepository ticketRepository;
    private final EventMapper eventMapper;

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

    public List<EventResponseDTO> getEventsByOrganizer(int organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream().map(this::toResponseWithSeats).toList();
    }

    public EventResponseDTO getEventById(int id) {
        return toResponseWithSeats(findEventOrThrow(id));
    }

    public EventResponseDTO createEvent(EventRequestDTO dto, int organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + organizerId));
        if (organizer.getStatus() == User.Status.BANNED) {
            throw new BusinessException("Un utente bannato non può creare eventi");
        }
        Event event = eventMapper.toEntity(dto);
        event.setOrganizer(organizer);
        event.setVenue(findVenueOrThrow(dto.getVenueId()));
        event.setTags(resolveTags(dto.getTagIds()));
        event.setSpeakers(resolveSpeakers(dto.getSpeakerIds()));
        return toResponseWithSeats(eventRepository.save(event));
    }

    public EventResponseDTO updateEvent(int eventId, EventRequestDTO dto, int requestingUserId) {
        Event event = findEventOrThrow(eventId);
        checkOwnershipOrAdmin(event, requestingUserId);
        eventMapper.updateEntity(dto, event);
        event.setVenue(findVenueOrThrow(dto.getVenueId()));
        event.setTags(resolveTags(dto.getTagIds()));
        event.setSpeakers(resolveSpeakers(dto.getSpeakerIds()));
        return toResponseWithSeats(eventRepository.save(event));
    }

    public void deleteEvent(int eventId, int requestingUserId) {
        Event event = findEventOrThrow(eventId);
        checkOwnershipOrAdmin(event, requestingUserId);
        eventRepository.deleteById(eventId);
    }

    private EventResponseDTO toResponseWithSeats(Event event) {
        EventResponseDTO dto = eventMapper.toResponseDTO(event);
        int bookedSeats = ticketRepository.countByEventIdAndStatus(event.getId(), Ticket.TicketStatus.ACTIVE);
        dto.setAvailableSeats(event.getVenue().getCapacity() - bookedSeats);
        return dto;
    }

    private void checkOwnershipOrAdmin(Event event, int requestingUserId) {
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + requestingUserId));
        boolean isOwner = event.getOrganizer().getId() == requestingUserId;
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Non hai i permessi per modificare questo evento");
        }
    }

    private Venue findVenueOrThrow(int venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede non trovata con id: " + venueId));
    }

    private List<Tag> resolveTags(List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return List.of();
        return tagIds.stream()
                .map(id -> tagRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag non trovato con id: " + id)))
                .toList();
    }

    private List<Speaker> resolveSpeakers(List<Integer> speakerIds) {
        if (speakerIds == null || speakerIds.isEmpty()) return List.of();
        return speakerIds.stream()
                .map(id -> speakerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Relatore non trovato con id: " + id)))
                .toList();
    }

    private Event findEventOrThrow(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + id));
    }
}
```

### TicketService.java
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.TicketRequestDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.entity.*;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.TicketMapper;
import com.academy.eventhub.repository.EventRepository;
import com.academy.eventhub.repository.TicketRepository;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    public TicketResponseDTO bookTicket(TicketRequestDTO dto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));
        if (user.getStatus() == User.Status.BANNED) {
            throw new BusinessException("Un utente bannato non può prenotare biglietti");
        }
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + dto.getEventId()));
        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Non puoi prenotare un evento già iniziato o passato");
        }
        if (event.getOrganizer().getId() == userId) {
            throw new BusinessException("Non puoi prenotare un evento che hai organizzato tu stesso");
        }
        if (ticketRepository.existsByUserIdAndEventId(userId, event.getId())) {
            throw new BusinessException("Hai già un biglietto per questo evento");
        }
        int bookedSeats = ticketRepository.countByEventIdAndStatus(event.getId(), Ticket.TicketStatus.ACTIVE);
        if (bookedSeats >= event.getVenue().getCapacity()) {
            throw new BusinessException("Non ci sono più posti disponibili per questo evento");
        }
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setEvent(event);
        ticket.setType(dto.getType());
        ticket.setStatus(Ticket.TicketStatus.ACTIVE);
        ticket.setPrice(dto.getType() == Ticket.TicketType.VIP ? event.getVipPrice() : event.getStandardPrice());
        return ticketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    public void cancelTicket(int ticketId, int userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Biglietto non trovato con id: " + ticketId));
        if (ticket.getUser().getId() != userId) {
            throw new BusinessException("Non puoi cancellare il biglietto di un altro utente");
        }
        if (ticket.getStatus() == Ticket.TicketStatus.CANCELLED) {
            throw new BusinessException("Il biglietto è già stato cancellato");
        }
        if (!LocalDateTime.now().isBefore(ticket.getEvent().getStartDate())) {
            throw new BusinessException("Non puoi cancellare un biglietto dopo l'inizio dell'evento");
        }
        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
    }

    public List<TicketResponseDTO> getUserTickets(int userId) {
        return ticketRepository.findByUserId(userId).stream().map(ticketMapper::toResponseDTO).toList();
    }

    public List<TicketResponseDTO> getEventParticipants(int eventId, int requestingUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + eventId));
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + requestingUserId));
        boolean isOwner = event.getOrganizer().getId() == requestingUserId;
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Non hai i permessi per vedere i partecipanti di questo evento");
        }
        return ticketRepository.findByEventId(eventId).stream().map(ticketMapper::toResponseDTO).toList();
    }
}
```

### FeedbackService.java
```java
package com.academy.eventhub.service;

import com.academy.eventhub.dto.FeedbackRequestDTO;
import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.entity.*;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.FeedbackMapper;
import com.academy.eventhub.repository.EventRepository;
import com.academy.eventhub.repository.FeedbackRepository;
import com.academy.eventhub.repository.TicketRepository;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final FeedbackMapper feedbackMapper;

    public FeedbackResponseDTO leaveFeedback(FeedbackRequestDTO dto, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + dto.getEventId()));
        if (!LocalDateTime.now().isAfter(event.getEndDate())) {
            throw new BusinessException("Puoi lasciare un feedback solo dopo la fine dell'evento");
        }
        ticketRepository.findByUserIdAndEventIdAndStatus(userId, event.getId(), Ticket.TicketStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Puoi lasciare un feedback solo se hai partecipato all'evento"));
        if (feedbackRepository.existsByUserIdAndEventId(userId, event.getId())) {
            throw new BusinessException("Hai già lasciato un feedback per questo evento");
        }
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setEvent(event);
        feedback.setRating(dto.getRating());
        feedback.setComment(dto.getComment());
        return feedbackMapper.toResponseDTO(feedbackRepository.save(feedback));
    }

    public List<FeedbackResponseDTO> getEventFeedbacks(int eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento non trovato con id: " + eventId);
        }
        return feedbackRepository.findByEventId(eventId).stream().map(feedbackMapper::toResponseDTO).toList();
    }

    public Double getEventRating(int eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento non trovato con id: " + eventId);
        }
        return feedbackRepository.findAverageRatingByEventId(eventId);
    }

    public void deleteFeedback(int feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback non trovato con id: " + feedbackId);
        }
        feedbackRepository.deleteById(feedbackId);
    }
}
```

---

## Controller (package `com.academy.eventhub.api`)

Regole generali:
- `@AuthenticationPrincipal CustomUserDetails userDetails` per ricavare l'utente autenticato
- `userDetails.getId()` per ricavare l'ID
- `@RequiredArgsConstructor` su ogni controller
- Documentazione Swagger completa su ogni endpoint

### AuthController.java
```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.SignupRequestDTO;
import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoint per la registrazione alla piattaforma")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Registra un nuovo utente", description = "Crea un nuovo account con ruolo USER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utente registrato con successo",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email già in uso", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Area personale e gestione utenti (ADMIN)")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Dati utente corrente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dati restituiti",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Non autenticato", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Lista tutti gli utenti", description = "Solo ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista utenti",
        content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/users/{id}")
    @Operation(summary = "Dettaglio utente per ID", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente trovato",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID utente") @PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/admin/users/{id}/promote")
    @Operation(summary = "Promuove un utente a ORGANIZER", description = "Solo ADMIN. Applicabile solo a utenti con ruolo USER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruolo aggiornato",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Utente non è USER", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> promoteUser(
            @Parameter(description = "ID utente da promuovere") @PathVariable int id) {
        return ResponseEntity.ok(userService.promoteUser(id));
    }

    @PutMapping("/admin/users/{id}/ban")
    @Operation(summary = "Banna un utente", description = "Solo ADMIN. Non applicabile agli ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente bannato",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Target è ADMIN o già bannato", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> banUser(
            @Parameter(description = "ID utente da bannare") @PathVariable int id) {
        return ResponseEntity.ok(userService.banUser(id));
    }

    @PutMapping("/admin/users/{id}/reactivate")
    @Operation(summary = "Riattiva un utente bannato", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente riattivato",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Utente non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Utente già attivo", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Gestione profilo dell'utente autenticato")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "Visualizza il proprio profilo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profilo trovato",
            content = @Content(schema = @Schema(implementation = UserProfileResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Profilo non ancora creato", content = @Content)
    })
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Crea il proprio profilo", description = "Può essere fatto una sola volta.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Profilo creato",
            content = @Content(schema = @Schema(implementation = UserProfileResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "409", description = "Profilo già esistente", content = @Content)
    })
    public ResponseEntity<UserProfileResponseDTO> createMyProfile(
            @Valid @RequestBody UserProfileRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.createProfile(userDetails.getId(), dto));
    }

    @PutMapping
    @Operation(summary = "Aggiorna il proprio profilo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profilo aggiornato",
            content = @Content(schema = @Schema(implementation = UserProfileResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "404", description = "Profilo non trovato", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Catalogo sedi — lettura pubblica, scrittura solo ADMIN")
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/venues")
    @Operation(summary = "Lista tutte le sedi")
    @ApiResponse(responseCode = "200", description = "Lista sedi",
        content = @Content(schema = @Schema(implementation = VenueResponseDTO.class)))
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/venues/{id}")
    @Operation(summary = "Dettaglio sede")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sede trovata",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sede non trovata", content = @Content)
    })
    public ResponseEntity<VenueResponseDTO> getVenueById(
            @Parameter(description = "ID sede") @PathVariable int id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @PostMapping("/admin/venues")
    @Operation(summary = "Crea una sede", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sede creata",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "409", description = "Sede con lo stesso nome già esistente", content = @Content)
    })
    public ResponseEntity<VenueResponseDTO> createVenue(@Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(dto));
    }

    @PutMapping("/admin/venues/{id}")
    @Operation(summary = "Modifica una sede", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sede aggiornata",
            content = @Content(schema = @Schema(implementation = VenueResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "404", description = "Sede non trovata", content = @Content)
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
        @ApiResponse(responseCode = "404", description = "Sede non trovata", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Catalogo categorie — lettura pubblica, scrittura solo ADMIN")
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    @Operation(summary = "Lista tutti i tag")
    @ApiResponse(responseCode = "200", description = "Lista tag",
        content = @Content(schema = @Schema(implementation = TagResponseDTO.class)))
    public ResponseEntity<List<TagResponseDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @PostMapping("/admin/tags")
    @Operation(summary = "Crea un tag", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tag creato",
            content = @Content(schema = @Schema(implementation = TagResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "409", description = "Tag con lo stesso nome già esistente", content = @Content)
    })
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody TagRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(dto));
    }

    @DeleteMapping("/admin/tags/{id}")
    @Operation(summary = "Elimina un tag", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tag eliminato"),
        @ApiResponse(responseCode = "404", description = "Tag non trovato", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Speakers", description = "Catalogo relatori — lettura pubblica, scrittura solo ADMIN")
public class SpeakerController {

    private final SpeakerService speakerService;

    @GetMapping("/speakers")
    @Operation(summary = "Lista tutti i relatori")
    @ApiResponse(responseCode = "200", description = "Lista relatori",
        content = @Content(schema = @Schema(implementation = SpeakerResponseDTO.class)))
    public ResponseEntity<List<SpeakerResponseDTO>> getAllSpeakers() {
        return ResponseEntity.ok(speakerService.getAllSpeakers());
    }

    @GetMapping("/speakers/{id}")
    @Operation(summary = "Dettaglio relatore")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatore trovato",
            content = @Content(schema = @Schema(implementation = SpeakerResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Relatore non trovato", content = @Content)
    })
    public ResponseEntity<SpeakerResponseDTO> getSpeakerById(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        return ResponseEntity.ok(speakerService.getSpeakerById(id));
    }

    @PostMapping("/admin/speakers")
    @Operation(summary = "Crea un relatore", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Relatore creato",
            content = @Content(schema = @Schema(implementation = SpeakerResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content)
    })
    public ResponseEntity<SpeakerResponseDTO> createSpeaker(@Valid @RequestBody SpeakerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.createSpeaker(dto));
    }

    @PutMapping("/admin/speakers/{id}")
    @Operation(summary = "Modifica un relatore", description = "Solo ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatore aggiornato",
            content = @Content(schema = @Schema(implementation = SpeakerResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "404", description = "Relatore non trovato", content = @Content)
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
        @ApiResponse(responseCode = "404", description = "Relatore non trovato", content = @Content)
    })
    public ResponseEntity<Void> deleteSpeaker(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        speakerService.deleteSpeaker(id);
        return ResponseEntity.noContent().build();
    }
}
```

### EventController.java

> **Nota:** `getAllEvents` accetta i parametri di filtro `date`, `tagId`, `venueId` come `@RequestParam` opzionali e li passa a `eventService.getAllEvents(date, tagId, venueId)`.

```java
package com.academy.eventhub.api;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.EventService;
import com.academy.eventhub.service.FeedbackService;
import com.academy.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Catalogo eventi, CRUD organizer e lista partecipanti")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;
    private final FeedbackService feedbackService;

    @GetMapping
    @Operation(summary = "Lista eventi pubblici (con filtri opzionali)")
    @ApiResponse(responseCode = "200", description = "Lista eventi",
        content = @Content(schema = @Schema(implementation = EventResponseDTO.class)))
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @RequestParam(required = false) LocalDateTime date,
            @RequestParam(required = false) Integer tagId,
            @RequestParam(required = false) Integer venueId) {
        return ResponseEntity.ok(eventService.getAllEvents(date, tagId, venueId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dettaglio evento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evento trovato",
            content = @Content(schema = @Schema(implementation = EventResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
    })
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID evento") @PathVariable int id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "I miei eventi", description = "ORGANIZER o ADMIN.")
    @ApiResponse(responseCode = "200", description = "Lista eventi dell'organizer",
        content = @Content(schema = @Schema(implementation = EventResponseDTO.class)))
    public ResponseEntity<List<EventResponseDTO>> getMyEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Crea un nuovo evento", description = "ORGANIZER o ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Evento creato",
            content = @Content(schema = @Schema(implementation = EventResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "404", description = "Venue, tag o speaker non trovati", content = @Content),
        @ApiResponse(responseCode = "409", description = "Utente bannato", content = @Content)
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
        @ApiResponse(responseCode = "200", description = "Evento aggiornato",
            content = @Content(schema = @Schema(implementation = EventResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dati non validi", content = @Content),
        @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento", content = @Content),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
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
        @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento", content = @Content),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.deleteEvent(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Lista partecipanti", description = "ORGANIZER (solo i propri eventi) o ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista biglietti",
            content = @Content(schema = @Schema(implementation = TicketResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento", content = @Content),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
    })
    public ResponseEntity<List<TicketResponseDTO>> getEventParticipants(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getEventParticipants(id, userDetails.getId()));
    }

    @GetMapping("/{id}/feedbacks")
    @Operation(summary = "Feedback di un evento", description = "Pubblica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista feedback",
            content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
    })
    public ResponseEntity<List<FeedbackResponseDTO>> getEventFeedbacks(
            @Parameter(description = "ID evento") @PathVariable int id) {
        return ResponseEntity.ok(feedbackService.getEventFeedbacks(id));
    }

    @GetMapping("/{id}/rating")
    @Operation(summary = "Valutazione media di un evento", description = "Pubblica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Media calcolata"),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content)
    })
    public ResponseEntity<Double> getEventRating(
            @Parameter(description = "ID evento") @PathVariable int id) {
        return ResponseEntity.ok(feedbackService.getEventRating(id));
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Prenotazione e cancellazione biglietti")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/tickets/my")
    @Operation(summary = "Le mie prenotazioni")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista biglietti",
            content = @Content(schema = @Schema(implementation = TicketResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Non autenticato", content = @Content)
    })
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getUserTickets(userDetails.getId()));
    }

    @PostMapping("/events/{eventId}/book")
    @Operation(summary = "Prenota un biglietto",
        description = "Prenota STANDARD o VIP. Il prezzo è impostato automaticamente. Vincoli: evento futuro, nessuna doppia prenotazione, posti disponibili, utente non bannato.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Biglietto prenotato",
            content = @Content(schema = @Schema(implementation = TicketResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Tipo biglietto non valido", content = @Content),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Evento passato, doppia prenotazione, posti esauriti o utente bannato", content = @Content)
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
    @Operation(summary = "Cancella una prenotazione",
        description = "Consentita solo prima dell'inizio dell'evento. Il biglietto viene marcato CANCELLED.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Prenotazione cancellata"),
        @ApiResponse(responseCode = "403", description = "Il biglietto non appartiene all'utente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Biglietto non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Biglietto già cancellato o evento già iniziato", content = @Content)
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Feedbacks", description = "Recensioni post-evento")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedbacks")
    @Operation(summary = "Lascia un feedback",
        description = "Vincoli: evento concluso, biglietto attivo presente, nessun feedback duplicato.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Feedback registrato",
            content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Voto non nel range 1-5", content = @Content),
        @ApiResponse(responseCode = "404", description = "Evento non trovato", content = @Content),
        @ApiResponse(responseCode = "409", description = "Evento non concluso, nessun biglietto valido o feedback già presente", content = @Content)
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
        @ApiResponse(responseCode = "404", description = "Feedback non trovato", content = @Content),
        @ApiResponse(responseCode = "403", description = "Accesso negato", content = @Content)
    })
    public ResponseEntity<Void> deleteFeedback(
            @Parameter(description = "ID feedback") @PathVariable int id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Frontend (static/)

### js/auth.js — funzioni globali disponibili

```javascript
const AUTH_KEY = 'eventhub_auth';
const USER_KEY = 'eventhub_user';

function getAuthHeader() {
    return sessionStorage.getItem(AUTH_KEY);
}

function getUser() {
    return JSON.parse(sessionStorage.getItem(USER_KEY));
}

function isLoggedIn() {
    return !!getAuthHeader();
}

function logout() {
    sessionStorage.removeItem(AUTH_KEY);
    sessionStorage.removeItem(USER_KEY);
    window.location.href = '/html/login.html';
}

async function apiFetch(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (isLoggedIn()) {
        headers['Authorization'] = getAuthHeader();
    }
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        logout();
        return null;
    }
    return response;
}

function renderNavbar(containerId = 'navbar') {
    const user = getUser();
    const navbar = document.getElementById(containerId);
    if (!navbar) return;

    let links = `<a href="/html/events.html">Eventi</a> `;

    if (user) {
        links += `<a href="/html/my-bookings.html">Le mie prenotazioni</a> `;
        links += `<a href="/html/profile.html">Profilo</a> `;
        if (user.role === 'ORGANIZER' || user.role === 'ADMIN') {
            links += `<a href="/html/organizer-events.html">I miei eventi</a> `;
        }
        if (user.role === 'ADMIN') {
            links += `<a href="/html/admin.html">Admin</a> `;
        }
        links += `<span>${user.email} [${user.role}]</span> `;
        links += `<button onclick="logout()">Logout</button>`;
    } else {
        links += `<a href="/html/login.html">Login</a> `;
        links += `<a href="/html/signup.html">Registrati</a>`;
    }

    navbar.innerHTML = links;
}
```

Chiavi `sessionStorage`: `eventhub_auth` (header Basic), `eventhub_user` (JSON utente)

### Pagine HTML

Tutte in `src/main/resources/static/html/`. Tutte importano `/js/auth.js`.
Accessibili da `http://localhost:8080/html/<pagina>.html`.

| File | Ruolo | Note |
|---|---|---|
| `login.html` | Tutti | Form login HTTP Basic; redirect per ruolo dopo login |
| `signup.html` | Tutti | POST /auth/signup |
| `events.html` | Tutti | Lista eventi con filtri data e tag |
| `event-detail.html` | Tutti | Dettaglio evento, form prenotazione (se loggato e evento futuro), lista feedback e rating |
| `my-bookings.html` | USER | Lista biglietti, cancellazione prenotazione, form feedback per eventi conclusi |
| `profile.html` | USER | Visualizza/aggiorna profilo (POST se non esiste, PUT se esiste) |
| `organizer-events.html` | ORGANIZER, ADMIN | Lista eventi propri, form crea/modifica, lista partecipanti |
| `admin.html` | ADMIN | Tab: Utenti (promuovi/banna/riattiva), Sedi (CRUD), Relatori (CRUD), Tag (crea/elimina) |

### CSS — `css/style.css`

Classi e ID rilevanti per future modifiche:

| Selettore | Uso |
|---|---|
| `#navbar` | Barra navigazione |
| `body:has(#loginForm)` | Centra la pagina (usato per login/signup) |
| `#loginForm` | Form login centrato |
| `#filters` | Barra filtri eventi (flex row) |
| `#eventsList`, `#myEventsList` | Card eventi (div bianchi con border) |
| `#eventForm` | Form evento organizer (div nascosto, max-width 640px) |
| `#participantsList` | Pannello partecipanti (sfondo #eff6ff) |
| `#tabs` + `.tab-content` | Sistema tab admin |
| `#usersList`, `#venuesList`, `#speakersList`, `#tagsList` | Liste admin (flex row) |
| `#venueForm`, `#speakerForm`, `#tagForm` | Form inline admin (flex wrap, input senza div wrapper) |
| `#venueMsg`, `#speakerMsg`, `#tagMsg`, `#eventFormMsg`, `#feedbackMsg` | Messaggi feedback (colore impostato via JS) |
| `button[onclick*="delete"]`, `button[onclick*="ban"]` | Bottoni distruttivi (rosso) |

---

## Matrice autorizzazioni

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
| `POST /events` | ORGANIZER o ADMIN |
| `PUT /events/{id}`, `DELETE /events/{id}` | ORGANIZER o ADMIN |
| `GET /events/my`, `GET /events/{id}/participants` | ORGANIZER o ADMIN |
| `/me/**` | autenticato |
| `POST /events/{eventId}/book`, `/tickets/**`, `/feedbacks/**` | autenticato |

---

## Test (package `com.academy.eventhub.service`)

### EventServiceTest.java — 12 test
- `getEventById`: trovato / non trovato
- `getAllEvents`: senza filtri / con filtro data
- `getEventsByOrganizer`: happy path
- `createEvent`: organizer attivo / organizer bannato / venue non trovata
- `updateEvent`: owner / admin / non autorizzato
- `deleteEvent`: owner / non autorizzato

### TicketServiceTest.java — 13 test
- `bookTicket`: happy path / utente bannato / evento passato / prenotazione proprio evento / doppia prenotazione / posti esauriti
- `cancelTicket`: happy path / biglietto altrui / già cancellato / evento già iniziato
- `getUserTickets`: happy path
- `getEventParticipants`: owner / admin / non autorizzato

---

## TODO post-consegna

- [ ] Migrazione da MySQL a PostgreSQL
- [ ] Docker Compose con PostgreSQL + Adminer

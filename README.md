# EventHub

Piattaforma per la gestione e prenotazione di eventi: conferenze, workshop, meetup e corsi di formazione.

## Repository

```bash
git clone https://github.com/mirto97/EventHub--LargeSystemsAcademy.git
cd EventHub--LargeSystemsAcademy
```

---

## Stack tecnico

| Componente | Tecnologia |
|---|---|
| Linguaggio | Java 21 |
| Framework | Spring Boot 4.0.6 |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Mapping DTO | MapStruct 1.6.3 |
| Boilerplate | Lombok 1.18.36 |
| Autenticazione | Spring Security 7 — HTTP Basic + BCrypt |
| Documentazione API | springdoc-openapi 3.0.3 |
| Build | Maven |
| Frontend | HTML5 + CSS3 + JavaScript vanilla (ES6+) |

---

## Prerequisiti

- Java 21+
- Maven 3.9+
- MySQL 8 installato e in esecuzione in locale

---

## Configurazione del database

Creare il database prima di avviare l'applicazione:

```sql
CREATE DATABASE eventhub;
```

Le credenziali di default configurate in `application.properties` sono:
URL:      jdbc:mysql://localhost:3306/eventhub
Username: root
Password: 1234

Se le tue credenziali MySQL sono diverse, modifica `src/main/resources/application.properties` prima di avviare.

Le tabelle vengono create automaticamente da Hibernate al primo avvio (`ddl-auto=update`).

---

## Avvio dell'applicazione

```bash
mvn spring-boot:run
```

L'applicazione sarà disponibile su `http://localhost:8080`.

---

## Frontend

Il frontend è accessibile direttamente dal browser:

| Pagina | URL | Ruolo |
|---|---|---|
| Login | `http://localhost:8080/html/login.html` | Tutti |
| Registrazione | `http://localhost:8080/html/signup.html` | Tutti |
| Catalogo eventi | `http://localhost:8080/html/events.html` | Tutti |
| Dettaglio evento | `http://localhost:8080/html/event-detail.html` | Tutti |
| Le mie prenotazioni | `http://localhost:8080/html/my-bookings.html` | USER |
| Profilo | `http://localhost:8080/html/profile.html` | USER |
| Gestione eventi | `http://localhost:8080/html/organizer-events.html` | ORGANIZER, ADMIN |
| Area admin | `http://localhost:8080/html/admin.html` | ADMIN |

---

## Credenziali demo

> Assicurarsi di inserire questi utenti nel database prima di testare, oppure usare l'endpoint `/auth/signup` per crearli e poi promuoverli manualmente.

| Ruolo | Email | Password |
|---|---|---|
| ADMIN | admin@eventhub.com | 123 |
| ORGANIZER | organizer@eventhub.com | 123 |
| USER | user@eventhub.com | 123 |

---

## Documentazione API (Swagger)

Swagger è disabilitato di default. Per abilitarlo, modificare `application.properties`:

```properties
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
```

Dopo il riavvio, la documentazione è disponibile su:
http://localhost:8080/swagger-ui.html

---

## Endpoint principali

### Pubblici
| Metodo | Endpoint | Descrizione |
|---|---|---|
| POST | `/auth/signup` | Registrazione nuovo utente |
| GET | `/events` | Lista eventi (filtri: `date`, `tagId`, `venueId`) |
| GET | `/events/{id}` | Dettaglio evento |
| GET | `/events/{id}/feedbacks` | Feedback di un evento |
| GET | `/events/{id}/rating` | Valutazione media di un evento |
| GET | `/venues` | Lista sedi |
| GET | `/tags` | Lista tag |
| GET | `/speakers` | Lista relatori |

### Autenticazione richiesta (USER)
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/me` | Dati utente corrente |
| GET | `/me/profile` | Visualizza profilo |
| POST | `/me/profile` | Crea profilo |
| PUT | `/me/profile` | Aggiorna profilo |
| POST | `/events/{id}/book` | Prenota un biglietto |
| GET | `/tickets/my` | Le mie prenotazioni |
| DELETE | `/tickets/{id}` | Cancella prenotazione |
| POST | `/feedbacks` | Lascia un feedback |

### ORGANIZER o ADMIN
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/events/my` | I miei eventi |
| POST | `/events` | Crea evento |
| PUT | `/events/{id}` | Modifica evento |
| DELETE | `/events/{id}` | Elimina evento |
| GET | `/events/{id}/participants` | Lista partecipanti |

### Solo ADMIN
| Metodo | Endpoint | Descrizione |
|---|---|---|
| GET | `/admin/users` | Lista utenti |
| PUT | `/admin/users/{id}/promote` | Promuovi a ORGANIZER |
| PUT | `/admin/users/{id}/ban` | Banna utente |
| PUT | `/admin/users/{id}/reactivate` | Riattiva utente |
| POST/PUT/DELETE | `/admin/venues` | Gestione sedi |
| POST/DELETE | `/admin/tags` | Gestione tag |
| POST/PUT/DELETE | `/admin/speakers` | Gestione relatori |
| DELETE | `/admin/feedbacks/{id}` | Rimuovi feedback |

---

## Struttura del progetto

**Backend** — `src/main/java/com/academy/eventhub/`
- `api/` — Controller REST
- `dto/` — DTO di request e response
- `entity/` — Entità JPA
- `exception/` — Eccezioni custom e GlobalExceptionHandler
- `mapper/` — Mapper MapStruct
- `repository/` — Repository Spring Data JPA
- `security/` — Configurazione Spring Security
- `service/` — Logica di business

**Frontend** — `src/main/resources/static/`
- `css/` — Fogli di stile
- `html/` — Pagine HTML
- `js/` — Script JavaScript

**Test** — `src/test/java/com/academy/eventhub/service/`
- `EventServiceTest.java`
- `TicketServiceTest.java`

## Esecuzione dei test

```bash
mvn test
```

I test unitari coprono `EventService` e `TicketService` con Mockito, senza dipendenze dal database.

---

## Regole di business principali

- Un utente non può prenotare più di un biglietto per lo stesso evento
- Non è possibile prenotare eventi già iniziati o passati
- La cancellazione di una prenotazione è consentita solo prima dell'inizio dell'evento
- Il feedback può essere lasciato solo dopo la fine dell'evento e solo da chi ha un biglietto valido
- Un utente bannato non può effettuare il login né creare prenotazioni
- Solo il creatore dell'evento o un ADMIN può modificarlo o eliminarlo

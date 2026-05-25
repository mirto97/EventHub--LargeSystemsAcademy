# EventHub — Progetto finale Academy 2026

**Durata:** 15 giorni lavorativi × 6 ore = **90 ore** totali
**Modalità:** individuale

---

## 1. Cos'è EventHub

EventHub è una piattaforma per **organizzare e prenotare eventi**: conferenze, workshop, meetup, corsi di formazione.

Su EventHub:
- chi organizza un evento (un'azienda, un formatore, un gruppo) lo **pubblica** indicando data, sede, capienza, relatori e argomenti;
- chi vuole partecipare lo **prenota** scegliendo il tipo di biglietto (standard o VIP);
- a fine evento, chi ha partecipato può **lasciare un feedback** che aiuta gli altri utenti a scegliere.

L'obiettivo è offrire un unico posto dove pubblicare eventi, gestirne le iscrizioni e raccoglierne il riscontro, senza ricorrere a strumenti separati (form Google, fogli Excel, gruppi WhatsApp).

### Perché questo progetto

Il progetto unisce in una sola applicazione tutto ciò che è stato visto in academy: gestione di dati relazionali complessi, autenticazione e ruoli, esposizione di API REST, validazione, documentazione automatica, frontend di consumo. È **la sintesi pratica del percorso**.

---

## 2. Gli attori della piattaforma

Tre ruoli, con permessi crescenti.

### 2.1 USER — l'utente registrato

Chiunque si iscriva alla piattaforma. Vuole trovare eventi interessanti e iscriversi.

**Cosa può fare:**
- Registrarsi con email e password, fare login/logout.
- Compilare e aggiornare il proprio **profilo** (nome, cognome, biografia, città, foto).
- **Sfogliare** il catalogo eventi pubblici, filtrare per data, categoria (tag), città.
- Vedere il **dettaglio** di un evento: descrizione, sede, relatori, posti disponibili, prezzo, feedback ricevuti.
- **Prenotare** un biglietto (standard o VIP) per un evento futuro.
- Consultare la lista delle **proprie prenotazioni**.
- **Cancellare** una prenotazione finché l'evento non è iniziato.
- **Lasciare un feedback** (voto 1-5 + commento) solo per eventi a cui ha effettivamente partecipato e che si sono conclusi.

### 2.2 ORGANIZER — l'organizzatore

Un utente promosso a organizzatore dall'admin. Vuole pubblicare e gestire i propri eventi.

**Cosa può fare** (oltre a tutto ciò che fa USER):
- **Creare** un nuovo evento: titolo, descrizione, data, sede (da catalogo), capienza, prezzi, tag, relatori associati.
- **Modificare** o **cancellare** i propri eventi (non quelli altrui).
- Vedere la lista dei **partecipanti** dei propri eventi.
- Vedere i **feedback ricevuti** sui propri eventi e la valutazione media.

### 2.3 ADMIN — l'amministratore

Il responsabile della piattaforma.

**Cosa può fare** (oltre a tutto):
- **Promuovere** un USER a ORGANIZER o revocare il ruolo.
- **Bannare** o riattivare utenti.
- Gestire il **catalogo sedi** (Venue): aggiungere, modificare, eliminare sedi con capienza e indirizzo.
- Gestire il **catalogo relatori** (Speaker): anagrafica condivisa tra eventi.
- Gestire il **catalogo categorie** (Tag).
- **Moderare** contenuti (rimuovere feedback inappropriati).

---

## 3. Esempi di scenari d'uso

Per fissare le idee, tre storie tipiche.

**Storia 1 — Mario, sviluppatore, si iscrive a una conferenza**
Mario apre EventHub, sfoglia gli eventi della prossima settimana filtrando per il tag "Java". Trova una conferenza interessante a Napoli, vede che ci saranno 3 relatori noti e che restano 12 posti standard. Prenota un biglietto VIP per avere il pranzo incluso. Riceve la conferma. Il giorno dopo l'evento, lascia un feedback di 5 stelle.

**Storia 2 — Acme S.r.l. organizza un workshop**
Acme è registrata come ORGANIZER. Crea un nuovo evento "Workshop Spring Boot", lo associa alla sede "Centro Direzionale - Sala A" (capienza 30) e a due relatori già presenti in catalogo. Pubblica l'evento. Nei giorni successivi monitora le iscrizioni dalla propria area organizer.

**Storia 3 — L'admin aggiunge una nuova sede**
L'admin riceve da Acme la richiesta di una nuova sede. La aggiunge al catalogo con indirizzo e capienza. Da quel momento qualunque organizer può associarvi i propri eventi.

---

## 4. Regole di business (vincoli funzionali)

Ogni regola va rispettata dall'applicazione. Sono regole, non suggerimenti.

1. **Capienza fisica.** Ogni evento ha una capienza massima ereditata dalla sede. Non si possono accettare prenotazioni oltre la capienza.
2. **No doppia prenotazione.** Lo stesso utente non può avere due biglietti attivi per lo stesso evento.
3. **No prenotazioni nel passato.** Non si possono creare prenotazioni per eventi la cui data di inizio è già trascorsa.
4. **Cancellazione consentita solo prima dell'evento.** Dopo l'inizio, il biglietto resta come "partecipazione".
5. **Feedback solo a posteriori.** Si può lasciare un feedback solo se: (a) l'evento si è concluso AND (b) l'utente aveva un biglietto valido.
6. **Un solo feedback per evento.** Lo stesso utente non può lasciare più feedback sullo stesso evento.
7. **Solo il creatore o l'admin** può modificare/cancellare un evento.
8. **Prezzi.** Biglietto VIP costa più di standard; entrambi configurabili per evento.
9. **Utente bannato.** Non può fare login né prenotare. I suoi biglietti futuri sono automaticamente cancellati.

---

## 5. Modello dati (vista alta)

Le entità principali e le loro relazioni. Il dettaglio implementativo lo decide ogni candidato.

| Entità | Descrizione | Relazioni principali |
|---|---|---|
| **User** | Utente della piattaforma | 1-1 con Profile, 1-N con Ticket, 1-N con Event (come organizer) |
| **Profile** | Dati anagrafici estesi dell'utente | 1-1 con User |
| **Role** | Ruolo (USER, ORGANIZER, ADMIN) | N-1 con User (oppure tabella di join) |
| **Venue** | Sede fisica con capienza | 1-N con Event |
| **Event** | Evento pubblicato | N-1 con Venue, N-1 con User (organizer), N-N con Tag, N-N con Speaker, 1-N con Ticket, 1-N con Feedback |
| **Tag** | Categoria/argomento dell'evento | N-N con Event |
| **Speaker** | Relatore (entità condivisa) | N-N con Event |
| **Ticket** | Prenotazione di un user a un evento. Contiene `type` (STANDARD/VIP), prezzo pagato, stato | N-1 con User, N-1 con Event |
| **Feedback** | Voto + commento post-evento | N-1 con User, N-1 con Event |

Mapping JPA da utilizzare (riferimento a quanto visto in academy):
- `@OneToOne` bi-directional (User ↔ Profile)
- `@OneToMany` / `@ManyToOne` (Venue → Event, User → Event, Event → Ticket, Event → Feedback)
- `@ManyToMany` (Event ↔ Tag, Event ↔ Speaker)
- **Ticket** è una "join entity" perché ha campi extra oltre alle due FK (tipo, prezzo, status).

---

## 6. Stack tecnico obbligatorio

Tutto quanto visto nei 18 giorni di teoria. **Non si usano librerie/framework non insegnati senza approvazione del mentor.**

### Backend
- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA + Hibernate**
- **Spring Security**: autenticazione JDBC + password con **BCrypt**, autorizzazione HTTP Basic con restrict URLs per ruolo.
- **Maven** per build e dipendenze.
- **PostgreSQL** come database, eseguito in **Docker**.
- **springdoc-openapi** per generare la documentazione Swagger UI.
- **Bean Validation** (`@Valid`, `@NotNull`, `@Size`, …) sulle DTO in ingresso.
- **Global exception handler** (`@RestControllerAdvice`) per risposte di errore uniformi.

### Frontend
- **HTML5 + CSS3 + JavaScript vanilla** (ES6+).
- **`fetch`** per chiamare le API.
- Niente framework (no React/Vue/Angular), niente bundler, niente Tailwind. Solo file `.html`, `.css`, `.js` statici.
- Il frontend serve **solo a dimostrare** che il backend funziona end-to-end. Non deve essere bello, deve essere chiaro e funzionante.

### Strumenti
- **Git** con repository personale su GitHub o GitLab.
- **Docker / Docker Compose** per avviare almeno il database PostgreSQL in locale.
- **Postman** per testare le API durante lo sviluppo e per la collection finale.

---

## 7. Frontend richiesto (minimo)

Set minimo di pagine HTML+CSS+JS:

| Pagina | A chi serve | Cosa mostra |
|---|---|---|
| `login.html` / `signup.html` | Tutti | Form di accesso/registrazione |
| `events.html` | Tutti (anche guest) | Lista eventi pubblici con filtri data/tag |
| `event-detail.html` | Tutti | Dettaglio + bottone "prenota" se loggato |
| `my-bookings.html` | USER | Le proprie prenotazioni + link per feedback |
| `profile.html` | USER | Visualizzazione/modifica profilo |
| `organizer-events.html` | ORGANIZER | Lista eventi propri + form crea/modifica + lista partecipanti |
| `admin.html` | ADMIN | Tab per gestire utenti, sedi, speaker, tag |

La navbar deve adattarsi al ruolo dell'utente loggato (mostrare/nascondere voci).

---

## 8. Roadmap giornaliera consigliata — 15 giorni × 6 ore

La roadmap riportata è un esempio e non deve essere considerata obbligatoria. Può però essere utile come riferimento.

## Backend foundation

### Step 1 — Setup e progettazione

1. Creare il repository Git.
2. Definire la branch strategy iniziale:
   - `main`
   - branch dedicate per le feature.
3. Creare il file `docker-compose.yml` con:
   - PostgreSQL
   - Adminer.
4. Inizializzare il progetto Spring Boot da start.spring.io.
5. Aggiungere le dipendenze principali:
   - Spring Web
   - Spring Data JPA
   - Spring Security
   - Validation
   - PostgreSQL Driver
   - springdoc / OpenAPI.
6. Disegnare l'ER diagram completo del modello dati.
7. Creare il README iniziale con:
   - prerequisiti
   - istruzioni di avvio
   - configurazione database
   - comandi Docker principali.

---

### Step 2 — Autenticazione e utenti

1. Creare l'entità `User`.
2. Creare l'entità `Role`.
3. Creare l'entità `Profile`.
4. Configurare la relazione `@OneToOne` bidirezionale tra `User` e `Profile`.
5. Configurare Spring Security con `JdbcUserDetailsManager`.
6. Creare o adattare le tabelle custom per la gestione degli utenti.
7. Configurare `BCryptPasswordEncoder`.
8. Implementare l'endpoint `POST /auth/signup`.
9. Configurare il login tramite HTTP Basic.
10. Testare in Postman:
    - registrazione utente standard
    - login utente standard
    - login admin.

---

### Step 3 — Gestione utenti e RBAC

1. Implementare il CRUD completo su `User`.
2. Introdurre DTO dedicati per input e output degli utenti.
3. Implementare il CRUD su `Profile`.
4. Creare l'endpoint `/me` per la gestione del profilo dell'utente autenticato.
5. Configurare le restrizioni URL per ruolo nella security config.
6. Limitare `/admin/**` agli utenti con ruolo ADMIN.
7. Creare lo scheletro di `@RestControllerAdvice`.
8. Gestire almeno:
   - `ResourceNotFoundException`
   - `ValidationException`.

---

### Step 4 — Sedi e ruoli

1. Creare l'entità `Venue`.
2. Aggiungere il campo capienza alla sede.
3. Implementare il CRUD di `Venue`.
4. Limitare il CRUD di `Venue` agli utenti ADMIN.
5. Implementare l'endpoint admin `PUT /admin/users/{id}/role` per la promozione o modifica del ruolo utente.
6. Completare la Bean Validation sulle DTO già presenti.

---

### Step 5 — Modello Event core

1. Creare l'entità `Event`.
2. Creare l'entità `Tag`.
3. Configurare la relazione tra `User` organizer ed `Event` con `@OneToMany`.
4. Configurare la relazione tra `Venue` ed `Event` con `@OneToMany`.
5. Configurare la relazione tra `Event` e `Tag` con `@ManyToMany`.
6. Implementare il CRUD base degli eventi.
7. Applicare le prime regole di accesso:
   - l'organizer può creare eventi
   - l'organizer può modificare i propri eventi
   - tutti possono leggere gli eventi.

---

## Backend completo

### Step 6 — Speaker e ricerca

1. Creare l'entità `Speaker`.
2. Configurare la relazione `@ManyToMany` tra `Event` e `Speaker`.
3. Implementare il CRUD di `Speaker`.
4. Limitare il CRUD di `Speaker` agli utenti ADMIN.
5. Implementare i filtri sulla lista eventi con Spring Data.
6. Gestire filtri per:
   - data
   - tag
   - venue
   - organizer.

---

### Step 7 — Prenotazioni

1. Creare l'entità `Ticket` come join entity.
2. Collegare `Ticket` a:
   - utente
   - evento.
3. Aggiungere il campo `type` con enum:
   - `STANDARD`
   - `VIP`.
4. Aggiungere il prezzo del ticket.
5. Aggiungere lo status del ticket.
6. Implementare la logica `availableSeats`.
7. Calcolare i posti disponibili come:
   - capienza della venue meno ticket attivi.
8. Implementare l'endpoint `POST /events/{id}/book`.
9. Gestire la scelta del tipo di biglietto in fase di prenotazione.
10. Implementare l'endpoint `DELETE /tickets/{id}` per la cancellazione della prenotazione.

---

### Step 8 — Regole di business

1. Impedire la prenotazione di eventi passati.
2. Impedire la doppia prenotazione dello stesso evento da parte dello stesso utente.
3. Completare la validazione su `Ticket`.
4. Completare la validazione su `Event`.
5. Completare il global exception handler.
6. Gestire risposte JSON ben formate per:
   - errori 400
   - errori 403
   - errori 404
   - errori 409.

---

### Step 9 — Feedback

1. Creare l'entità `Feedback`.
2. Aggiungere rating da 1 a 5.
3. Aggiungere il commento testuale.
4. Implementare il CRUD di `Feedback`.
5. Consentire il feedback solo se l'evento è concluso.
6. Consentire il feedback solo se l'utente possiede un ticket valido.
7. Impedire recensioni duplicate per lo stesso evento da parte dello stesso utente.
8. Implementare l'endpoint `GET /events/{id}/rating`.
9. Calcolare e restituire la media dei voti dell'evento.

---

### Step 10 — Rifinitura backend

1. Aggiungere paginazione su `GET /events` usando `Pageable`.
2. Aggiungere sorting su `GET /events`.
3. Aggiungere annotazioni OpenAPI sui controller.
4. Organizzare la documentazione Swagger con tag e descrizioni.
5. Rifattorizzare il codice separando chiaramente:
   - Entity
   - DTO
   - Mapper
   - Repository
   - Service
   - Controller.

---

## Frontend, test, consegna

### Step 11 — Frontend setup e autenticazione

1. Creare la cartella `src/main/resources/static/`.
2. Creare `index.html`.
3. Creare il CSS base condiviso.
4. Creare `login.html`.
5. Creare `signup.html`.
6. Collegare i form di login e signup alle API backend.
7. Gestire l'autenticazione Basic salvando l'header `Authorization` in `sessionStorage`.
8. Creare una navbar dinamica in base al ruolo dell'utente autenticato.

---

### Step 12 — Frontend utente

1. Creare `events.html`.
2. Mostrare la lista degli eventi.
3. Aggiungere filtri per data.
4. Aggiungere filtri per tag.
5. Creare `event-detail.html`.
6. Mostrare il dettaglio completo dell'evento.
7. Aggiungere il bottone di prenotazione.
8. Creare il form di prenotazione.
9. Consentire la scelta del tipo di ticket:
   - `STANDARD`
   - `VIP`.
10. Collegare il form di prenotazione all'API backend.

---

### Step 13 — Frontend organizer e admin

1. Creare `organizer-events.html`.
2. Mostrare la lista degli eventi creati dall'organizer.
3. Aggiungere il form di creazione evento.
4. Aggiungere il form di modifica evento.
5. Creare `admin.html`.
6. Aggiungere la gestione utenti.
7. Implementare l'azione di promozione utente.
8. Implementare l'eventuale azione di ban utente.
9. Aggiungere la gestione delle venue.
10. Creare `my-bookings.html`.
11. Mostrare le prenotazioni dell'utente autenticato.
12. Aggiungere il link al feedback per gli eventi conclusi.

---

### Step 14 — Test, Docker e documentazione

1. Creare una Postman collection completa.
2. Esportare la collection in `/docs/postman/`.
3. Aggiungere test JUnit sul service layer.
4. Coprire almeno:
   - `EventService`
   - `TicketService`.
5. Finalizzare il file `docker-compose.yml`.
6. Verificare l'avvio completo del progetto partendo da zero con Docker Compose.
7. Completare il README finale con:
   - setup del progetto
   - credenziali demo
   - comandi principali
   - screenshot
   - descrizione delle funzionalità.

---

### Step 15 — Buffer e demo

1. Correggere bug residui.
2. Rifattorizzare il codice dove necessario.
3. Pulire codice inutilizzato, endpoint provvisori e configurazioni non più necessarie.
4. Preparare una presentazione o uno script demo.
5. Verificare il flusso completo della demo:
   - signup
   - login
   - creazione evento
   - prenotazione
   - feedback
   - gestione admin.
6. Presentare il progetto al mentor.

---

## Checklist finale

- [ ] Repository Git inizializzato correttamente.
- [ ] Branch strategy rispettata.
- [ ] Ambiente Docker funzionante.
- [ ] Backend Spring Boot avviabile da zero.
- [ ] Database PostgreSQL configurato.
- [ ] Autenticazione funzionante.
- [ ] Ruoli e autorizzazioni configurati.
- [ ] CRUD principali completati.
- [ ] Regole di business implementate.
- [ ] Gestione errori centralizzata.
- [ ] Swagger/OpenAPI disponibile.
- [ ] Frontend statico funzionante.
- [ ] Postman collection esportata.
- [ ] Test JUnit principali presenti.
- [ ] README completo.
- [ ] Demo pronta.


## 9. Deliverable finale

Al termine del 15° giorno ogni candidato deve consegnare:

1. **Repository Git** pubblico (o con accesso garantito ai mentor) contenente:
   - Codice sorgente backend e frontend.
   - `docker-compose.yml` funzionante.
2. **Swagger UI** raggiungibile su `/swagger-ui.html` ad applicazione avviata.
3. **Presentazione finale**:
   - Demo live del flusso completo (signup → prenotazione → feedback).


---


## 10. Domande frequenti

**Posso usare Lombok?** Sì.

**Posso usare un frontend framework?** No. È un vincolo del progetto.

**Cosa succede se non finisco?** Si valuta quello che hai fatto, secondo la griglia. Un MVP solido vale più di un progetto ambizioso e rotto.

**Posso aiutare un collega?** Discussione concettuale: sì. Scambio di codice: no.


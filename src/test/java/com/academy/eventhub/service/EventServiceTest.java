package com.academy.eventhub.service;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.entity.*;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.EventMapper;
import com.academy.eventhub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith dice a JUnit di usare Mockito per gestire i @Mock e @InjectMocks
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    // ── MOCK delle dipendenze ──────────────────────────────────
    // Mockito crea versioni "finte" di questi oggetti.
    // Non parlano con il DB — rispondono solo a quello che definiamo nei test.

    @Mock private EventRepository eventRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagRepository tagRepository;
    @Mock private SpeakerRepository speakerRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private EventMapper eventMapper;

    // @InjectMocks crea il service REALE e inietta i mock sopra come dipendenze.
    // È come se Spring facesse il constructor injection, ma in un test.
    @InjectMocks
    private EventService eventService;

    // ── Oggetti di test riutilizzabili ─────────────────────────
    // Dichiaro qui le variabili che uso in più test,
    // così non le ricreo ogni volta da zero.
    private User organizer;
    private User adminUser;
    private User otherUser;
    private Venue venue;
    private Event event;
    private EventResponseDTO eventResponseDTO;

    // @BeforeEach viene eseguito prima di OGNI test.
    // Qui costruisco gli oggetti "standard" da cui i test partono.
    @BeforeEach
    void setUp() {
        // Organizer: utente attivo con ruolo ORGANIZER
        organizer = new User();
        organizer.setId(1);
        organizer.setEmail("organizer@test.com");
        organizer.setRole(User.Role.ORGANIZER);
        organizer.setStatus(User.Status.ACTIVE);

        // Admin: utente con ruolo ADMIN
        adminUser = new User();
        adminUser.setId(2);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.Status.ACTIVE);

        // Utente generico che non è proprietario dell'evento
        otherUser = new User();
        otherUser.setId(3);
        otherUser.setEmail("other@test.com");
        otherUser.setRole(User.Role.USER);
        otherUser.setStatus(User.Status.ACTIVE);

        // Sede con capienza 100
        venue = new Venue();
        venue.setId(1);
        venue.setName("Sala Test");
        venue.setAddress("Via Test 1");
        venue.setCapacity(100);

        // Evento futuro associato all'organizer e alla venue
        event = new Event();
        event.setId(1);
        event.setTitle("Evento Test");
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setStartDate(LocalDateTime.now().plusDays(7));  // nel futuro
        event.setEndDate(LocalDateTime.now().plusDays(7).plusHours(3));
        event.setStandardPrice(BigDecimal.valueOf(50));
        event.setVipPrice(BigDecimal.valueOf(100));
        event.setTags(new ArrayList<>());
        event.setSpeakers(new ArrayList<>());
        event.setTickets(new ArrayList<>());
        event.setFeedbacks(new ArrayList<>());

        // DTO di risposta che il mapper "restituirà" nei test
        eventResponseDTO = new EventResponseDTO();
        eventResponseDTO.setId(1);
        eventResponseDTO.setTitle("Evento Test");
        eventResponseDTO.setAvailableSeats(100);
    }

    // ── Metodo di supporto ─────────────────────────────────────
    // Costruisce un EventRequestDTO valido per i test di create/update.
    private EventRequestDTO buildEventRequest() {
        EventRequestDTO dto = new EventRequestDTO();
        dto.setTitle("Evento Test");
        dto.setStartDate(LocalDateTime.now().plusDays(7));
        dto.setEndDate(LocalDateTime.now().plusDays(7).plusHours(3));
        dto.setStandardPrice(BigDecimal.valueOf(50));
        dto.setVipPrice(BigDecimal.valueOf(100));
        dto.setVenueId(1);
        dto.setTagIds(List.of());
        dto.setSpeakerIds(List.of());
        return dto;
    }

    // ── Metodo di supporto ─────────────────────────────────────
    // toResponseWithSeats viene chiamato internamente da quasi tutti i metodi
    // del service. Questo helper configura i mock necessari perché funzioni.
    private void mockToResponseWithSeats() {
        // quando il mapper converte Event → EventResponseDTO, restituisce il nostro DTO di test
        when(eventMapper.toResponseDTO(event)).thenReturn(eventResponseDTO);
        // quando si contano i biglietti attivi per l'evento, restituisce 0 (nessuna prenotazione)
        when(ticketRepository.countByEventIdAndStatus(event.getId(), Ticket.TicketStatus.ACTIVE))
                .thenReturn(0);
    }

    // ════════════════════════════════════════════════════════════
    // TEST: getEventById
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getEventById: evento trovato → restituisce EventResponseDTO")
    void getEventById_whenEventExists_returnsEventResponseDTO() {
        // ARRANGE — preparo i mock:
        // quando la repository cerca l'id 1, restituisce il nostro evento
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        mockToResponseWithSeats();

        // ACT — chiamo il metodo reale del service
        EventResponseDTO result = eventService.getEventById(1);

        // ASSERT — verifico che il risultato sia quello atteso
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getTitle()).isEqualTo("Evento Test");
    }

    @Test
    @DisplayName("getEventById: evento non trovato → lancia ResourceNotFoundException")
    void getEventById_whenEventNotFound_throwsResourceNotFoundException() {
        // ARRANGE — la repository non trova niente
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        // ACT + ASSERT — assertThrows verifica che il metodo lanci esattamente questa eccezione.
        // Se non la lancia (o ne lancia un'altra), il test fallisce.
        assertThrows(
            ResourceNotFoundException.class,
            () -> eventService.getEventById(99)
        );
    }

    // ════════════════════════════════════════════════════════════
    // TEST: getAllEvents
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllEvents: senza filtri → restituisce tutti gli eventi")
    void getAllEvents_withNoFilters_returnsAllEvents() {
        // ARRANGE
        when(eventRepository.findAll()).thenReturn(List.of(event));
        mockToResponseWithSeats();

        // ACT — null, null, null = nessun filtro
        List<EventResponseDTO> result = eventService.getAllEvents(null, null, null);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Evento Test");
    }

    @Test
    @DisplayName("getAllEvents: con filtro data → usa findByStartDateAfter")
    void getAllEvents_withDateFilter_usesDateRepository() {
        // ARRANGE
        LocalDateTime filterDate = LocalDateTime.now();
        when(eventRepository.findByStartDateAfter(filterDate)).thenReturn(List.of(event));
        mockToResponseWithSeats();

        // ACT
        List<EventResponseDTO> result = eventService.getAllEvents(filterDate, null, null);

        // ASSERT
        assertThat(result).hasSize(1);
        // verify controlla che findByStartDateAfter sia stato chiamato esattamente 1 volta
        verify(eventRepository, times(1)).findByStartDateAfter(filterDate);
    }

    // ════════════════════════════════════════════════════════════
    // TEST: getEventsByOrganizer
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getEventsByOrganizer: restituisce gli eventi dell'organizer")
    void getEventsByOrganizer_returnsOrganizerEvents() {
        // ARRANGE
        when(eventRepository.findByOrganizerId(organizer.getId())).thenReturn(List.of(event));
        mockToResponseWithSeats();

        // ACT
        List<EventResponseDTO> result = eventService.getEventsByOrganizer(organizer.getId());

        // ASSERT
        assertThat(result).hasSize(1);
    }

    // ════════════════════════════════════════════════════════════
    // TEST: createEvent
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createEvent: organizer attivo, dati validi → crea e restituisce l'evento")
    void createEvent_whenOrganizerIsActive_returnsCreatedEvent() {
        // ARRANGE
        EventRequestDTO dto = buildEventRequest();

        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(venueRepository.findById(1)).thenReturn(Optional.of(venue));
        // toEntity crea un Event "grezzo" dal DTO (senza organizer e venue, li imposta il service)
        when(eventMapper.toEntity(dto)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        mockToResponseWithSeats();

        // ACT
        EventResponseDTO result = eventService.createEvent(dto, organizer.getId());

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Evento Test");
        // verify che save sia stato chiamato — conferma che l'evento è stato persistito
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("createEvent: organizer bannato → lancia BusinessException")
    void createEvent_whenOrganizerIsBanned_throwsBusinessException() {
        // ARRANGE — imposto l'organizer come BANNED
        organizer.setStatus(User.Status.BANNED);
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));

        EventRequestDTO dto = buildEventRequest();

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> eventService.createEvent(dto, organizer.getId())
        );

        // verify che save NON sia mai stato chiamato (l'evento non deve essere salvato)
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("createEvent: venue non trovata → lancia ResourceNotFoundException")
    void createEvent_whenVenueNotFound_throwsResourceNotFoundException() {
        // ARRANGE — l'organizer esiste ma la venue no
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(eventMapper.toEntity(any())).thenReturn(event);
        when(venueRepository.findById(1)).thenReturn(Optional.empty());

        EventRequestDTO dto = buildEventRequest();

        // ACT + ASSERT
        assertThrows(
            ResourceNotFoundException.class,
            () -> eventService.createEvent(dto, organizer.getId())
        );
    }

    // ════════════════════════════════════════════════════════════
    // TEST: updateEvent
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateEvent: richiesta dal proprietario → aggiorna l'evento")
    void updateEvent_whenRequesterIsOwner_returnsUpdatedEvent() {
        // ARRANGE
        EventRequestDTO dto = buildEventRequest();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        // checkOwnershipOrAdmin chiama userRepository per trovare il richiedente
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(venueRepository.findById(1)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        mockToResponseWithSeats();

        // ACT — organizer.getId() = 1, event.getOrganizer().getId() = 1 → è il proprietario
        EventResponseDTO result = eventService.updateEvent(event.getId(), dto, organizer.getId());

        // ASSERT
        assertThat(result).isNotNull();
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("updateEvent: richiesta dall'admin → aggiorna l'evento anche se non è il proprietario")
    void updateEvent_whenRequesterIsAdmin_returnsUpdatedEvent() {
        // ARRANGE
        EventRequestDTO dto = buildEventRequest();

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        // l'admin ha id=2, l'organizer ha id=1 → non è il proprietario, ma è ADMIN
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(venueRepository.findById(1)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        mockToResponseWithSeats();

        // ACT
        EventResponseDTO result = eventService.updateEvent(event.getId(), dto, adminUser.getId());

        // ASSERT
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updateEvent: richiesta da utente non autorizzato → lancia BusinessException")
    void updateEvent_whenRequesterIsNeitherOwnerNorAdmin_throwsBusinessException() {
        // ARRANGE — otherUser (id=3) non è proprietario (id=1) né admin
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        EventRequestDTO dto = buildEventRequest();

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> eventService.updateEvent(event.getId(), dto, otherUser.getId())
        );

        verify(eventRepository, never()).save(any());
    }

    // ════════════════════════════════════════════════════════════
    // TEST: deleteEvent
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteEvent: richiesta dal proprietario → elimina l'evento")
    void deleteEvent_whenRequesterIsOwner_deletesEvent() {
        // ARRANGE
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));

        // ACT
        eventService.deleteEvent(event.getId(), organizer.getId());

        // ASSERT — verify che deleteById sia stato chiamato con l'id corretto
        verify(eventRepository, times(1)).deleteById(event.getId());
    }

    @Test
    @DisplayName("deleteEvent: richiesta da utente non autorizzato → lancia BusinessException")
    void deleteEvent_whenRequesterIsNotOwnerOrAdmin_throwsBusinessException() {
        // ARRANGE
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> eventService.deleteEvent(event.getId(), otherUser.getId())
        );

        // l'evento NON deve essere eliminato
        verify(eventRepository, never()).deleteById(any());
    }
}
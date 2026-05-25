package com.academy.eventhub.service;

import com.academy.eventhub.dto.TicketRequestDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.entity.*;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.mapper.TicketMapper;
import com.academy.eventhub.repository.EventRepository;
import com.academy.eventhub.repository.TicketRepository;
import com.academy.eventhub.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private TicketMapper ticketMapper;

    @InjectMocks
    private TicketService ticketService;

    // ── Oggetti riutilizzabili ─────────────────────────────────
    private User user;
    private User organizer;
    private User adminUser;
    private Venue venue;
    private Event futureEvent;   // evento non ancora iniziato (prenotabile)
    private Event pastEvent;     // evento già iniziato (non prenotabile)
    private Ticket activeTicket;
    private TicketResponseDTO ticketResponseDTO;

    @BeforeEach
    void setUp() {
        // Utente normale attivo
        user = new User();
        user.setId(1);
        user.setEmail("user@test.com");
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);

        // Organizer dell'evento (id diverso dall'utente, importante per il test "own event")
        organizer = new User();
        organizer.setId(2);
        organizer.setEmail("organizer@test.com");
        organizer.setRole(User.Role.ORGANIZER);
        organizer.setStatus(User.Status.ACTIVE);

        // Admin
        adminUser = new User();
        adminUser.setId(3);
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.Status.ACTIVE);

        // Sede con capienza 50
        venue = new Venue();
        venue.setId(1);
        venue.setName("Sala Test");
        venue.setCapacity(50);

        // Evento nel FUTURO → prenotabile
        futureEvent = new Event();
        futureEvent.setId(1);
        futureEvent.setTitle("Evento Futuro");
        futureEvent.setOrganizer(organizer);
        futureEvent.setVenue(venue);
        futureEvent.setStartDate(LocalDateTime.now().plusDays(10));
        futureEvent.setEndDate(LocalDateTime.now().plusDays(10).plusHours(2));
        futureEvent.setStandardPrice(BigDecimal.valueOf(30));
        futureEvent.setVipPrice(BigDecimal.valueOf(80));
        futureEvent.setTickets(new ArrayList<>());

        // Evento NEL PASSATO → non prenotabile né cancellabile
        pastEvent = new Event();
        pastEvent.setId(2);
        pastEvent.setTitle("Evento Passato");
        pastEvent.setOrganizer(organizer);
        pastEvent.setVenue(venue);
        pastEvent.setStartDate(LocalDateTime.now().minusDays(1));
        pastEvent.setEndDate(LocalDateTime.now().minusHours(1));
        pastEvent.setStandardPrice(BigDecimal.valueOf(30));
        pastEvent.setVipPrice(BigDecimal.valueOf(80));

        // Biglietto ATTIVO dell'utente per l'evento futuro
        activeTicket = new Ticket();
        activeTicket.setId(1);
        activeTicket.setUser(user);
        activeTicket.setEvent(futureEvent);
        activeTicket.setType(Ticket.TicketType.STANDARD);
        activeTicket.setStatus(Ticket.TicketStatus.ACTIVE);
        activeTicket.setPrice(BigDecimal.valueOf(30));

        // DTO di risposta che il mapper restituirà
        ticketResponseDTO = new TicketResponseDTO();
        ticketResponseDTO.setId(1);
        ticketResponseDTO.setType(Ticket.TicketType.STANDARD);
        ticketResponseDTO.setStatus(Ticket.TicketStatus.ACTIVE);
    }

    // ── Metodo di supporto ─────────────────────────────────────
    private TicketRequestDTO buildRequest(Ticket.TicketType type, int eventId) {
        TicketRequestDTO dto = new TicketRequestDTO();
        dto.setType(type);
        dto.setEventId(eventId);
        return dto;
    }

    // ════════════════════════════════════════════════════════════
    // TEST: bookTicket
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("bookTicket: utente attivo, evento futuro, posti disponibili → crea il biglietto")
    void bookTicket_whenAllConditionsMet_returnsTicketResponseDTO() {
        // ARRANGE
        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, futureEvent.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        // nessuna prenotazione duplicata
        when(ticketRepository.existsByUserIdAndEventId(user.getId(), futureEvent.getId())).thenReturn(false);
        // 0 posti occupati su 50 disponibili
        when(ticketRepository.countByEventIdAndStatus(futureEvent.getId(), Ticket.TicketStatus.ACTIVE)).thenReturn(0);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(activeTicket);
        when(ticketMapper.toResponseDTO(activeTicket)).thenReturn(ticketResponseDTO);

        // ACT
        TicketResponseDTO result = ticketService.bookTicket(dto, user.getId());

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(Ticket.TicketType.STANDARD);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("bookTicket: utente bannato → lancia BusinessException")
    void bookTicket_whenUserIsBanned_throwsBusinessException() {
        // ARRANGE — imposto l'utente come BANNED
        user.setStatus(User.Status.BANNED);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, futureEvent.getId());

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.bookTicket(dto, user.getId())
        );

        // il biglietto NON deve essere salvato
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookTicket: evento già iniziato → lancia BusinessException")
    void bookTicket_whenEventAlreadyStarted_throwsBusinessException() {
        // ARRANGE — uso pastEvent (startDate nel passato)
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(pastEvent.getId())).thenReturn(Optional.of(pastEvent));

        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, pastEvent.getId());

        // ACT + ASSERT
        // il service controlla: event.getStartDate().isAfter(LocalDateTime.now())
        // pastEvent ha startDate nel passato → la condizione è falsa → BusinessException
        assertThrows(
            BusinessException.class,
            () -> ticketService.bookTicket(dto, user.getId())
        );
    }

    @Test
    @DisplayName("bookTicket: l'utente prova a prenotare il proprio evento → lancia BusinessException")
    void bookTicket_whenUserIsOrganizer_throwsBusinessException() {
        // ARRANGE — organizer (id=2) prova a prenotare il suo stesso evento
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));

        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, futureEvent.getId());

        // ACT + ASSERT
        // futureEvent.getOrganizer().getId() == 2 == organizer.getId() → BusinessException
        assertThrows(
            BusinessException.class,
            () -> ticketService.bookTicket(dto, organizer.getId())
        );
    }

    @Test
    @DisplayName("bookTicket: utente ha già un biglietto → lancia BusinessException")
    void bookTicket_whenUserAlreadyHasTicket_throwsBusinessException() {
        // ARRANGE
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        // existsByUserIdAndEventId restituisce true → prenotazione già esistente
        when(ticketRepository.existsByUserIdAndEventId(user.getId(), futureEvent.getId())).thenReturn(true);

        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, futureEvent.getId());

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.bookTicket(dto, user.getId())
        );
    }

    @Test
    @DisplayName("bookTicket: nessun posto disponibile → lancia BusinessException")
    void bookTicket_whenNoSeatsAvailable_throwsBusinessException() {
        // ARRANGE
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        when(ticketRepository.existsByUserIdAndEventId(user.getId(), futureEvent.getId())).thenReturn(false);
        // venue.capacity = 50, biglietti attivi = 50 → sold out
        when(ticketRepository.countByEventIdAndStatus(futureEvent.getId(), Ticket.TicketStatus.ACTIVE)).thenReturn(50);

        TicketRequestDTO dto = buildRequest(Ticket.TicketType.STANDARD, futureEvent.getId());

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.bookTicket(dto, user.getId())
        );
    }

    // ════════════════════════════════════════════════════════════
    // TEST: cancelTicket
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("cancelTicket: biglietto attivo, evento non ancora iniziato → imposta status CANCELLED")
    void cancelTicket_whenAllConditionsMet_setsStatusCancelled() {
        // ARRANGE — activeTicket ha event = futureEvent (nel futuro)
        when(ticketRepository.findById(activeTicket.getId())).thenReturn(Optional.of(activeTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(activeTicket);

        // ACT
        ticketService.cancelTicket(activeTicket.getId(), user.getId());

        // ASSERT — verifico che il biglietto sia stato salvato con status CANCELLED
        // ArgumentCaptor sarebbe più preciso, ma qui basta verify
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        assertThat(activeTicket.getStatus()).isEqualTo(Ticket.TicketStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelTicket: il biglietto appartiene a un altro utente → lancia BusinessException")
    void cancelTicket_whenTicketBelongsToOtherUser_throwsBusinessException() {
        // ARRANGE — activeTicket.user.id = 1, ma il richiedente è l'organizer (id=2)
        when(ticketRepository.findById(activeTicket.getId())).thenReturn(Optional.of(activeTicket));

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.cancelTicket(activeTicket.getId(), organizer.getId())
        );
    }

    @Test
    @DisplayName("cancelTicket: biglietto già cancellato → lancia BusinessException")
    void cancelTicket_whenTicketAlreadyCancelled_throwsBusinessException() {
        // ARRANGE — imposto il biglietto come già CANCELLED
        activeTicket.setStatus(Ticket.TicketStatus.CANCELLED);
        when(ticketRepository.findById(activeTicket.getId())).thenReturn(Optional.of(activeTicket));

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.cancelTicket(activeTicket.getId(), user.getId())
        );
    }

    @Test
    @DisplayName("cancelTicket: evento già iniziato → lancia BusinessException")
    void cancelTicket_whenEventAlreadyStarted_throwsBusinessException() {
        // ARRANGE — cambio l'evento del biglietto con uno passato
        activeTicket.setEvent(pastEvent);
        when(ticketRepository.findById(activeTicket.getId())).thenReturn(Optional.of(activeTicket));

        // ACT + ASSERT
        // il service controlla: LocalDateTime.now().isBefore(ticket.getEvent().getStartDate())
        // pastEvent.startDate è nel passato → la condizione è falsa → BusinessException
        assertThrows(
            BusinessException.class,
            () -> ticketService.cancelTicket(activeTicket.getId(), user.getId())
        );
    }

    // ════════════════════════════════════════════════════════════
    // TEST: getUserTickets
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUserTickets: restituisce la lista di biglietti dell'utente")
    void getUserTickets_returnsUserTickets() {
        // ARRANGE
        when(ticketRepository.findByUserId(user.getId())).thenReturn(List.of(activeTicket));
        when(ticketMapper.toResponseDTO(activeTicket)).thenReturn(ticketResponseDTO);

        // ACT
        List<TicketResponseDTO> result = ticketService.getUserTickets(user.getId());

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
    }

    // ════════════════════════════════════════════════════════════
    // TEST: getEventParticipants
    // ════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getEventParticipants: richiesta dal proprietario → restituisce i biglietti")
    void getEventParticipants_whenRequesterIsOwner_returnsTickets() {
        // ARRANGE
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(ticketRepository.findByEventId(futureEvent.getId())).thenReturn(List.of(activeTicket));
        when(ticketMapper.toResponseDTO(activeTicket)).thenReturn(ticketResponseDTO);

        // ACT — organizer.getId() = 2 = futureEvent.getOrganizer().getId() → è il proprietario
        List<TicketResponseDTO> result = ticketService.getEventParticipants(futureEvent.getId(), organizer.getId());

        // ASSERT
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getEventParticipants: richiesta dall'admin → restituisce i biglietti")
    void getEventParticipants_whenRequesterIsAdmin_returnsTickets() {
        // ARRANGE
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(ticketRepository.findByEventId(futureEvent.getId())).thenReturn(List.of(activeTicket));
        when(ticketMapper.toResponseDTO(activeTicket)).thenReturn(ticketResponseDTO);

        // ACT — adminUser non è il proprietario ma è ADMIN
        List<TicketResponseDTO> result = ticketService.getEventParticipants(futureEvent.getId(), adminUser.getId());

        // ASSERT
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getEventParticipants: utente non autorizzato → lancia BusinessException")
    void getEventParticipants_whenRequesterIsNotOwnerOrAdmin_throwsBusinessException() {
        // ARRANGE — user (id=1) non è il proprietario (id=2) né admin
        when(eventRepository.findById(futureEvent.getId())).thenReturn(Optional.of(futureEvent));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // ACT + ASSERT
        assertThrows(
            BusinessException.class,
            () -> ticketService.getEventParticipants(futureEvent.getId(), user.getId())
        );
    }
}
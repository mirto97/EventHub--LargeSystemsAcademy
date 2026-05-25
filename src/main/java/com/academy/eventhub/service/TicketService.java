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
    
    /**
     * crea un ticket e lo associa allo user che recuperiamo dall'id in input
     * @param dto
     * @param userId
     * @return ticketdto nuovo
     */
    public TicketResponseDTO bookTicket(TicketRequestDTO dto, int userId) {
        // recupera lo user se non lo trova lancio exc
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));

        // controlla se non è bannato, se no lancio exc
        if (user.getStatus() == User.Status.BANNED) {
            throw new BusinessException("Un utente bannato non può prenotare biglietti");
        }

        // recupera l'event, se non lo trova lancio exc
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + dto.getEventId()));

        // l'evento non deve essere già iniziato
        if (!event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Non puoi prenotare un evento già iniziato o passato");
        }

        // l'utente non può prenotare il proprio evento (nel caso sia un'organizzatore)
        if (event.getOrganizer().getId() == userId) {
            throw new BusinessException("Non puoi prenotare un evento che hai organizzato tu stesso");
        }

        // no doppia prenotazione
        if (ticketRepository.existsByUserIdAndEventId(userId, event.getId())) {
            throw new BusinessException("Hai già un biglietto per questo evento");
        }

        // controllo posti disponibili
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

        // salva entità, mappa a dto e lo restituisce
        return ticketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    /**
     * "cancella" il ticket associato allo user, recuperandoli dagli id in input
     * @param ticketId
     * @param userId
     */
    public void cancelTicket(int ticketId, int userId) {
        // recupera il ticket, se non lo trova lancio exc
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Biglietto non trovato con id: " + ticketId));

        // controlla lo user che sta facendo l'operazione è lo stesso associato al ticket, se no lancio exc
        if (ticket.getUser().getId() != userId) {
            throw new BusinessException("Non puoi cancellare il biglietto di un altro utente");
        }

        // il ticket non deve essere già cancellato
        if (ticket.getStatus() == Ticket.TicketStatus.CANCELLED) {
            throw new BusinessException("Il biglietto è già stato cancellato");
        }

        // controllare se l'evento non è già iniziato, se no lancio exc
        if (!LocalDateTime.now().isBefore(ticket.getEvent().getStartDate())) {
            throw new BusinessException("Non puoi cancellare un biglietto dopo l'inizio dell'evento");
        }

        // gli cambio lo status e lo salvo aggiornato
        ticket.setStatus(Ticket.TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
    }

    /**
     * recupera i tickets da uno user, che recupero dallo userid in input
     * @param userId
     * @return lista di ticketdto
     */
    public List<TicketResponseDTO> getUserTickets(int userId) {
        return ticketRepository.findByUserId(userId)
                .stream()
                .map(ticketMapper::toResponseDTO)   // .map(ticket -> ticketMapper.toResponseDTO(ticket))
                .toList();
    }

    /**
     * recupera tutti i ticket dei partecipanti di un event, che recuperiamo dall'eventid in input 
     * @param eventId
     * @param requestingUserId userid di chi fa la richiesta per il controllo
     * @return lista di ticketdto
     */
    public List<TicketResponseDTO> getEventParticipants(int eventId, int requestingUserId) {
        // controlla se l'event esiste, se no lancio exc
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + eventId));

        // recupera lo user che fa la richiesta, se non lo trova lancio exc
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + requestingUserId));

        // controllo se lo user è l'organizer dell'evento o un admin, se non lo è lancio exc
        boolean isOwner = event.getOrganizer().getId() == requestingUserId;
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Non hai i permessi per vedere i partecipanti di questo evento");
        }

        return ticketRepository.findByEventId(eventId)
                .stream()
                .map(ticketMapper::toResponseDTO)   // .map(ticket -> ticketMapper.toResponseDTO(ticket))
                .toList();
    }
}
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

    /**
     * vedo tutti gli eventi filtrati a seconda degli input 
     * @param date
     * @param tagId
     * @param venueId
     * @return events filtrati
     */
    public List<EventResponseDTO> getAllEvents(LocalDateTime date, Integer tagId, Integer venueId) {
        List<Event> events = new ArrayList<>();

        // se l'input mi ha fornito un tagId, recupero tutto il tag
        Tag tag = null;
        if (tagId != null) {
            tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag non trovato con id: " + tagId));
        }

        // Usa il metodo repository più specifico disponibile per date e tag
        if (date != null && tag != null) {
            events = eventRepository.findByStartDateAfterAndTagsContaining(date, tag);
        } else if (date != null) {
            events = eventRepository.findByStartDateAfter(date);
        } else if (tag != null) {
            events = eventRepository.findByTagsContaining(tag);
        } else if (venueId != null) {
            // per venue usa il metodo dedicato, dopo il resto
            events = eventRepository.findByVenueId(venueId);
        } else {
            // se in input non mi ha dato nulla, carico tutti gli events
            events = eventRepository.findAll();
        }

        // Se venueId è presente combinato con altri filtri, applica in stream
        // (non esiste un metodo repository per tutte le combinazioni)
        if (venueId != null && (date != null || tag != null)) {
            int vid = venueId;
            events = events.stream()
                    .filter(e -> e.getVenue().getId() == vid)
                    .toList();
        }

        // restituisco gli eventi con i posti rimasti
        return events.stream()
                .map(this::toResponseWithSeats)     // applica il metodo toResponseWithSeats a ogni elemento
                .toList();
    }

    /**
     * cerca gli events dell'organizer che corrisponde all'id in input
     * @param organizerId
     * @return dtos specifici
     */
    public List<EventResponseDTO> getEventsByOrganizer(int organizerId) {
        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(this::toResponseWithSeats)     // .map(event -> this.toResponseWithSeats(event))
                .toList();
    }

    /**
     * uso findEventOrThrow per restituire l'evento che corrisponde all'id in input
     * @param id
     * @return dto
     */
    public EventResponseDTO getEventById(int id) {
        return toResponseWithSeats(findEventOrThrow(id));
    }

    /**
     * crea l'evento 
     * @param dto
     * @param organizerId
     * @return dto nuovo
     */
    public EventResponseDTO createEvent(EventRequestDTO dto, int organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + organizerId));

        // se l'organizer è bannato lancio exc
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

    /**
     * aggiorno l'evento
     * @param eventId
     * @param dto
     * @param requestingUserId
     * @return dto aggiornato
     */
    public EventResponseDTO updateEvent(int eventId, EventRequestDTO dto, int requestingUserId) {
        Event event = findEventOrThrow(eventId);
        // controllo se può modificarlo
        checkOwnershipOrAdmin(event, requestingUserId);

        eventMapper.updateEntity(dto, event);
        event.setVenue(findVenueOrThrow(dto.getVenueId()));
        event.setTags(resolveTags(dto.getTagIds()));
        event.setSpeakers(resolveSpeakers(dto.getSpeakerIds()));

        return toResponseWithSeats(eventRepository.save(event));
    }

    /**
     * cancella l'evento
     * @param eventId
     * @param requestingUserId
     */
    public void deleteEvent(int eventId, int requestingUserId) {
        Event event = findEventOrThrow(eventId);
        checkOwnershipOrAdmin(event, requestingUserId);
        eventRepository.deleteById(eventId);
    }

    // --- Helper privati ---

    /**
     * da entity a dto calcolando i posti rimanenti
     * @param event
     * @return dto con posti rimanenti
     */
    private EventResponseDTO toResponseWithSeats(Event event) {
        EventResponseDTO dto = eventMapper.toResponseDTO(event);
        // calcolo quanti biglietti attivi ci sono per quell'evento
        int bookedSeats = ticketRepository.countByEventIdAndStatus(event.getId(), Ticket.TicketStatus.ACTIVE);
        // li sottraggo alla capacità massima del posto e ottengo i posti rimanenti
        dto.setAvailableSeats(event.getVenue().getCapacity() - bookedSeats);
        return dto;
    }

    /**
     * controllo se l'id in input è di un admin o dell'organizzatore dell'evento in input
     * servirà per controllare se l'utente può modificare l'evento
     * @param event
     * @param requestingUserId
     */
    private void checkOwnershipOrAdmin(Event event, int requestingUserId) {
        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + requestingUserId));

        boolean isOwner = event.getOrganizer().getId() == requestingUserId;
        boolean isAdmin = requester.getRole() == User.Role.ADMIN;

        // se non è nè l'organizzatore nè l'admin lancio exc
        if (!isOwner && !isAdmin) {
            throw new BusinessException("Non hai i permessi per modificare questo evento");
        }
    }

    /**
     * recupero il venue dall'id in input oppure lancio exc
     * @param venueId
     * @return venue
     */
    private Venue findVenueOrThrow(int venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede non trovata con id: " + venueId));
    }

    /**
     * recupero una serie di tags da una serie di id in input
     * @param tagIds
     * @return tags richiesti
     */
    private List<Tag> resolveTags(List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) 
            return List.of();
        return tagIds.stream()
                .map(id -> tagRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag non trovato con id: " + id)))
                .toList();
    }

    /**
     * recupero una serie di speakers da una serie di id in input
     * @param speakerIds
     * @return speakers richiesti
     */
    private List<Speaker> resolveSpeakers(List<Integer> speakerIds) {
        if (speakerIds == null || speakerIds.isEmpty()) return List.of();
        return speakerIds.stream()
                .map(id -> speakerRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Relatore non trovato con id: " + id)))
                .toList();
    }

    /**
     * recupero l'evento dall'id in input oppure lancio exc
     * @param id
     * @return event richiesto
     */
    private Event findEventOrThrow(int id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + id));
    }
}
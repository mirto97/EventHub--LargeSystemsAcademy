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

    /**
     * crea un feedback e lo associa all'user che recuperiamo dall'userid in input 
     * @param dto
     * @param userId
     * @return feedbackdto nuovo
     */
    public FeedbackResponseDTO leaveFeedback(FeedbackRequestDTO dto, int userId) {
        // recupero user o lancio exc
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));

        // uguale con event
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con id: " + dto.getEventId()));

        // l'evento deve essere concluso
        if (!LocalDateTime.now().isAfter(event.getEndDate())) {
            throw new BusinessException("Puoi lasciare un feedback solo dopo la fine dell'evento");
        }

        // l'utente deve avere un biglietto attivo per quell'evento
        ticketRepository.findByUserIdAndEventIdAndStatus(userId, event.getId(), Ticket.TicketStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Puoi lasciare un feedback solo se hai partecipato all'evento"));

        // no feedback duplicato
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

    /**
     * recupera i feedback associati all'event che corrisponde all'eventid in input
     * @param eventId
     * @return feedbackdto legati all'event
     */
    public List<FeedbackResponseDTO> getEventFeedbacks(int eventId) {
        // controllo se l'event esiste
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento non trovato con id: " + eventId);
        }

        // restituisco la lista di dto
        return feedbackRepository.findByEventId(eventId)
                .stream()
                .map(feedbackMapper::toResponseDTO)     // .map(feedback -> feedbackMapper.toResponseDTO(feedback))
                .toList();
    }

    /**
     * restituisce la media delle valutazioni dell'evento associato all'eventid in input
     * @param eventId
     * @return
     */
    public Double getEventRating(int eventId) {
        // controlla se l'evento esiste se no lancio exc
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Evento non trovato con id: " + eventId);
        }
        // uso il metodo per prendere la media
        return feedbackRepository.findAverageRatingByEventId(eventId);
    }

    /**
     * cancella un feedback
     * @param feedbackId
     */
    public void deleteFeedback(int feedbackId) {
        // controlla se l'evento esiste se no lancio exc
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new ResourceNotFoundException("Feedback non trovato con id: " + feedbackId);
        }
        feedbackRepository.deleteById(feedbackId);
    }
}
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    // biglietti di un utente
    List<Ticket> findByUserId(int userId);

    // biglietti di un evento
    List<Ticket> findByEventId(int eventId);

    // controlla doppia prenotazione
    boolean existsByUserIdAndEventId(int userId, int eventId);

    // conta i biglietti attivi di un evento (per calcolare i posti disponibili)
    int countByEventIdAndStatus(int eventId, Ticket.TicketStatus status);

    // cerca il biglietto attivo di un utente per un evento (serve per validare il feedback)
    Optional<Ticket> findByUserIdAndEventIdAndStatus(int userId, int eventId, Ticket.TicketStatus status);
}
package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // feedback di un evento
    List<Feedback> findByEventId(int eventId);

    // feedback di un utente
    List<Feedback> findByUserId(int userId);

    // controlla feedback duplicato
    boolean existsByUserIdAndEventId(int userId, int eventId);

    // calcola e recupera la media voti di un evento
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.event.id = :eventId")
    Double findAverageRatingByEventId(@Param("eventId") int eventId);
}
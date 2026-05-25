package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Event;
import com.academy.eventhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    // eventi di un organizer
    List<Event> findByOrganizerId(int organizerId);

    // eventi futuri
    List<Event> findByStartDateAfter(LocalDateTime date);

    // eventi per sede
    List<Event> findByVenueId(int venueId);

    // eventi che contengono un certo tag
    List<Event> findByTagsContaining(Tag tag);

    // eventi futuri filtrati per tag
    List<Event> findByStartDateAfterAndTagsContaining(LocalDateTime date, Tag tag);
}
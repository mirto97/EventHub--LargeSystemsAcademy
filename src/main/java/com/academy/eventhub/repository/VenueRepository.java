package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Integer> {

    boolean existsByName(String name);
}
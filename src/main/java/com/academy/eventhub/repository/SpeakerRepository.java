package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Speaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeakerRepository extends JpaRepository<Speaker, Integer> {
}
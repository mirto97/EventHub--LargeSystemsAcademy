package com.academy.eventhub.repository;

import com.academy.eventhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);
}
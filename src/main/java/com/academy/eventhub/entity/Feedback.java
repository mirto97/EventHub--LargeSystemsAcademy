package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT") // permetto che sia anche parecchio lungo
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
package com.academy.eventhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il titolo è obbligatorio")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "La data di fine è obbligatoria")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @NotNull(message = "Il prezzo standard è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal standardPrice;

    @NotNull(message = "Il prezzo VIP è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal vipPrice;

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToMany
    @JoinTable(
        name = "event_tag",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "event_speaker",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "speaker_id")
    )
    private List<Speaker> speakers = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();



    // Validazione cross-field: endDate deve essere dopo startDate
    @AssertTrue(message = "La data di fine deve essere successiva alla data di inizio")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) 
            return true; // perchè è già gestito da @NotNull, quindi in caso non do un doppio errore
        return endDate.isAfter(startDate);
    }
}
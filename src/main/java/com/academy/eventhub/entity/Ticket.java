package com.academy.eventhub.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Il tipo di biglietto è obbligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;
    public enum TicketType { STANDARD, VIP }

    @NotNull(message = "Il prezzo è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ACTIVE;
    public enum TicketStatus { ACTIVE, CANCELLED }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
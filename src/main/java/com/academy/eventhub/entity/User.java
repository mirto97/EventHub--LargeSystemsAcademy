package com.academy.eventhub.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
// @Data genera equals()/hashCode() basati su TUTTI i campi, incluse le relazioni lazy.
// Questo causa problemi con Hibernate: quando accede a una relazione non ancora caricata
// (lazy proxy), può lanciare LazyInitializationException o produrre loop infiniti.
// Regola: su @Entity usare sempre @Getter + @Setter separati.
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Email(message = "Email non valida")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    // ho fatto un enum, per semplificare
    @Enumerated(EnumType.STRING)
    private Role role;
    public enum Role { USER, ORGANIZER, ADMIN };

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    public enum Status { ACTIVE, BANNED }



}

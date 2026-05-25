package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FeedbackMapper {

    // MapStruct supporta la notazione con il punto per navigare le relazioni.
    // "event.id" significa: prendo l'oggetto event dentro Ticket, e da lì prendo il campo id. Non devo scrivere codice extra.
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    FeedbackResponseDTO toResponseDTO(Feedback feedback);
}
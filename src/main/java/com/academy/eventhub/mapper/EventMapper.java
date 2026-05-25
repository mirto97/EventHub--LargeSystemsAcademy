package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {VenueMapper.class, UserMapper.class, TagMapper.class, SpeakerMapper.class})
public interface EventMapper {

    @Mapping(target = "availableSeats", ignore = true)
    EventResponseDTO toResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "speakers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "feedbacks", ignore = true)
    Event toEntity(EventRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "speakers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "feedbacks", ignore = true)
    void updateEntity(EventRequestDTO dto, @MappingTarget Event event);

    // campi ignorati perché richiedono una query al database, non posso ricavarli dal DTO direttamente
    // sarà il service a recuperarli dai repository e ad impostarli sull'entity dopo la mappatura
}
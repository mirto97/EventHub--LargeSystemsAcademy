package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    VenueResponseDTO toResponseDTO(Venue venue);

    @Mapping(target = "id", ignore = true)
    Venue toEntity(VenueRequestDTO dto);


    @Mapping(target = "id", ignore = true)
    void updateEntity(VenueRequestDTO dto, @MappingTarget Venue venue);
    // @MappingTarget non crea un nuovo oggetto, aggiorna quello esistente
    // copia i campi del dto dentro all'entità
    // infatti ignoriamo l'id perchè deve rimanere invariato
}
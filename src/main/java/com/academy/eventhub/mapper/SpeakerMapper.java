package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.entity.Speaker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SpeakerMapper {
    SpeakerResponseDTO toResponseDTO(Speaker speaker);
    
    @Mapping(target = "id", ignore = true)
    Speaker toEntity(SpeakerRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    void updateEntity(SpeakerRequestDTO dto, @MappingTarget Speaker speaker);
}
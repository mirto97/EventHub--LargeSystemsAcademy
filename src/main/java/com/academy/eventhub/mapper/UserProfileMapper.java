package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfileResponseDTO toResponseDTO(UserProfile profile);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserProfile toEntity(UserProfileRequestDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(UserProfileRequestDTO dto, @MappingTarget UserProfile profile);
}
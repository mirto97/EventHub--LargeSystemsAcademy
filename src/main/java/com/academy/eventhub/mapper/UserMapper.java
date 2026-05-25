package com.academy.eventhub.mapper;

import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}
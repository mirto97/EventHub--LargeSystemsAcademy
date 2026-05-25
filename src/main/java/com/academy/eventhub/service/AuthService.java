package com.academy.eventhub.service;

import com.academy.eventhub.dto.SignupRequestDTO;
import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.entity.User;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.mapper.UserMapper;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * registra un nuovo utente
     * @param dto
     * @return user nuovo
     */
    public UserResponseDTO signup(SignupRequestDTO dto) {
        // non possono esserci duplicati di email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email già in uso: " + dto.getEmail());
        }

        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);

        return userMapper.toResponseDTO(userRepository.save(user));
    }
}
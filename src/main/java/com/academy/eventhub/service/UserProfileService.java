package com.academy.eventhub.service;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.entity.User;
import com.academy.eventhub.entity.UserProfile;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.UserProfileMapper;
import com.academy.eventhub.repository.UserProfileRepository;
import com.academy.eventhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    
    /**
     * recupera uno userprofile, se non lo trova lancio exc
     * @param userId
     * @return userprofile
     */
    private UserProfile findProfileOrThrow(int userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profilo non trovato per l'utente con id: " + userId));
    }

    /**
     * recupera lo userprofile dallo user, che recuperiamo dallo userid con cui usiamo findProfileOrThrow
     * @param userId
     * @return userprofiledto 
     */
    public UserProfileResponseDTO getProfileByUserId(int userId) {
        return userProfileMapper.toResponseDTO(findProfileOrThrow(userId));
    }

    /**
     * crea uno userprofile e lo associa allo user, che recuperiamo dallo userid
     * @param userId
     * @param dto
     * @return userprofiledto
     */
    public UserProfileResponseDTO createProfile(int userId, UserProfileRequestDTO dto) {
        // recupero lo user, se non lo trova lancio exc
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con id: " + userId));

        // controllo che non esista già
        if (userProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException("Il profilo per questo utente esiste già");
        }

        // mappa il dto a entity e lo associa lo userprofile allo user
        UserProfile profile = userProfileMapper.toEntity(dto);
        profile.setUser(user);

        // salva l'entità, la mappa a dto e la restituisce
        return userProfileMapper.toResponseDTO(userProfileRepository.save(profile));
    }

    /**
     * aggiorna uno userprofile associato allo user, che recuperiamo dallo userid in input
     * @param userId
     * @param dto
     * @return userprofiledto
     */
    public UserProfileResponseDTO updateProfile(int userId, UserProfileRequestDTO dto) {
        // controlla se esiste, uso findProfileOrThrow
        UserProfile profile = findProfileOrThrow(userId);
        // TODO aggiorna l'entità, mappando il dto in input a entità (ma mi sa bisogna comunque salvarla l'entità)
        userProfileMapper.updateEntity(dto, profile);
        // salva l'entità, la mappa a dto e la restituisce
        return userProfileMapper.toResponseDTO(userProfileRepository.save(profile));
    }
}
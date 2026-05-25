package com.academy.eventhub.service;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.entity.Speaker;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.SpeakerMapper;
import com.academy.eventhub.repository.SpeakerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeakerService {
    
    private final SpeakerRepository speakerRepository;
    private final SpeakerMapper speakerMapper;
    
    /**
     * recupera tutti gli speakers
     * @return
    */
   public List<SpeakerResponseDTO> getAllSpeakers() {
       return speakerRepository.findAll()
       .stream()
       .map(speakerMapper::toResponseDTO)
       .toList();
    }
    
    /**
     * recupera lo speaker dallo speakerid in input, se non lo trova lancio exc
     * @param id
     * @return speaker
     */
    private Speaker findSpeakerOrThrow(int id) {
        return speakerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relatore non trovato con id: " + id));
    }

    /**
     * recupera lo speaker dallo speakerid in input, uso findSpeakerOrThrow
     * @param id
     * @return speakerdto
     */
    public SpeakerResponseDTO getSpeakerById(int id) {
        return speakerMapper.toResponseDTO(findSpeakerOrThrow(id));
    }

    /**
     * crea uno speaker
     * @param dto
     * @return speakerdto nuovo
     */
    public SpeakerResponseDTO createSpeaker(SpeakerRequestDTO dto) {
        // -requestdto in input lo mappa in entità, lo salva, poi lo mappa a -responsedto e lo restituisce
        return speakerMapper.toResponseDTO(speakerRepository.save(speakerMapper.toEntity(dto)));
    }

    /**
     * aggiorna uno speaker associato dall'id in input
     * @param id
     * @param dto
     * @return speakerdto aggiornato
     */
    public SpeakerResponseDTO updateSpeaker(int id, SpeakerRequestDTO dto) {
        // vede se esiste dall'id
        Speaker speaker = findSpeakerOrThrow(id);
        // aggiorno
        speakerMapper.updateEntity(dto, speaker);
        // restituisce
        return speakerMapper.toResponseDTO(speakerRepository.save(speaker));
    }

    /**
     * cancella lo speaker associato all'id in input
     * @param id
     */
    public void deleteSpeaker(int id) {
        // vede se esiste
        findSpeakerOrThrow(id);
        // cancella
        speakerRepository.deleteById(id);
    }

}
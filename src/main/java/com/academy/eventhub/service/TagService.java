package com.academy.eventhub.service;

import com.academy.eventhub.dto.TagRequestDTO;
import com.academy.eventhub.dto.TagResponseDTO;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.TagMapper;
import com.academy.eventhub.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    
    /**
     * recupera tutti i tag 
     * @return lista di tagdto
     */
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toResponseDTO)  // .map(tag -> tagMapper.toRespondeDTO(tag))
                .toList();
    }

    /**
     * crea un tag
     * @param dto
     * @return
     */
    public TagResponseDTO createTag(TagRequestDTO dto) {
        // controlla se esiste già con questo nome
        if (tagRepository.existsByName(dto.getName())) {
            throw new BusinessException("Esiste già un tag con il nome: " + dto.getName());
        }

        // -requestdto in input lo mappa in entità, lo salva, poi lo mappa a -responsedto e lo restituisce
        return tagMapper.toResponseDTO(tagRepository.save(tagMapper.toEntity(dto)));
    }

    /**
     * cancella lo speaker associato all'id in input
     * @param id
     */
    public void deleteTag(int id) {
        // controlla se esiste se no lancio exc
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag non trovato con id: " + id);
        }
        // cancella
        tagRepository.deleteById(id);
    }
}
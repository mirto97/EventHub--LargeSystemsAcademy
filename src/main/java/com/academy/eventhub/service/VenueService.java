package com.academy.eventhub.service;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.entity.Venue;
import com.academy.eventhub.exception.BusinessException;
import com.academy.eventhub.exception.ResourceNotFoundException;
import com.academy.eventhub.mapper.VenueMapper;
import com.academy.eventhub.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;
    
    /**
     * recupero il venue dal venueid in input, se non lo trova lancio exc
     * @param id
     * @return venue
     */
    private Venue findVenueOrThrow(int id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede non trovata con id: " + id));
    }

    /**
     * recupera tutti i venue 
     * @return lista venuedtos
    */
    public List<VenueResponseDTO> getAllVenues() {
       return venueRepository.findAll()
                .stream()
                .map(venueMapper::toResponseDTO)    // .map(venue -> venueMapper.toResponseDTO(venue))
                .toList();
    }

    /**
     * recupera il venuedto dal venueid, uso findVenueOrThrow
     * @param id
     * @return venuedto
     */
    public VenueResponseDTO getVenueById(int id) {
        return venueMapper.toResponseDTO(findVenueOrThrow(id));
    }

    /**
     * crea un venue da un venuedto in input
     * @param dto
     * @return venuedto nuovo
     */
    public VenueResponseDTO createVenue(VenueRequestDTO dto) {
        // controllo che non ci siano nomi duplicati, se no lancio exc
        if (venueRepository.existsByName(dto.getName())) {
            throw new BusinessException("Esiste già una sede con il nome: " + dto.getName());
        }

        // -requestdto mappato a entity e salvata entità, poi mappo a -responsedto e restituisco 
        return venueMapper.toResponseDTO(venueRepository.save(venueMapper.toEntity(dto)));
    }

    /**
     * aggiorna il venue che corrisponde all'id in input
     * @param id
     * @param dto
     * @return venuedto aggiornato
     */
    public VenueResponseDTO updateVenue(int id, VenueRequestDTO dto) {
        // controllo che esiste con findVenueOrThrow
        Venue venue = findVenueOrThrow(id);
        // aggiorno l'entità col dto in input
        venueMapper.updateEntity(dto, venue);
        // salvo entità aggiornata, mappo a dto e restituisco
        return venueMapper.toResponseDTO(venueRepository.save(venue));
    }

    /**
     * cancello il venue che corrisponde all'id in input
     * @param id
     */
    public void deleteVenue(int id) {
        // controllo che esiste con findVenueOrThrow
        findVenueOrThrow(id);
        venueRepository.deleteById(id);
    }

}
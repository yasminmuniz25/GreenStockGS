package com.greenstock.ms.inventario.service;

import com.greenstock.ms.inventario.clients.OfertasClient;
import com.greenstock.ms.inventario.dtos.LoteRequestDTO;
import com.greenstock.ms.inventario.dtos.LoteResponseDTO;
import com.greenstock.ms.inventario.dtos.StatusLoteRequestDTO;
import com.greenstock.ms.inventario.dtos.StatusOfertaRequestDTO;
import com.greenstock.ms.inventario.entities.Lote;
import com.greenstock.ms.inventario.entities.StatusLote;
import com.greenstock.ms.inventario.exceptions.ResourceNotFoundException;
import com.greenstock.ms.inventario.repositories.LoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private OfertasClient ofertasClient;

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> findAllLotes() {
        List<Lote> lotes = loteRepository.findAll();
        return lotes.stream().map(LoteResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public LoteResponseDTO findLoteById(Long id){
        Lote lote = loteRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Recurso não encontrado. ID: "+ id)
        );
        return new LoteResponseDTO(lote);
    }

    @Transactional
    public LoteResponseDTO saveLote(LoteRequestDTO inputDTO){
        Lote lote = new Lote();
        mapDtoToLote(inputDTO, lote);
        lote = loteRepository.save(lote);
        return new LoteResponseDTO(lote);
    }

    private void mapDtoToLote(LoteRequestDTO inputDTO, Lote lote){
        lote.setNomeProduto(inputDTO.getNomeProduto());
        lote.setCodigoDeBarras(inputDTO.getCodigoDeBarras());
        lote.setQuantidade(inputDTO.getQuantidade());
        lote.setDataValidade(inputDTO.getDataValidade());
        lote.setStatusLote(inputDTO.getStatusLote());
    }


    @Transactional
    public LoteResponseDTO updateLote(Long id, LoteRequestDTO inputDTO){
        try {
            Lote lote = loteRepository.getReferenceById(id);


            mapDtoToLote(inputDTO, lote);

            lote = loteRepository.save(lote);
            return new LoteResponseDTO(lote);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
    }

    @Transactional
    public LoteResponseDTO atualizarStatus(Long id, StatusLoteRequestDTO dto) {
        Lote lote = loteRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado. ID: " + id));
        lote.setStatusLote(dto.getStatusLote());
        lote = loteRepository.save(lote);

        if (dto.getStatusLote() == StatusLote.VENCIDO) {
            ofertasClient.atualizarStatusPorLote(id, new StatusOfertaRequestDTO("EXPIRADA"));
        } else if (dto.getStatusLote() == StatusLote.ESGOTADO) {
            ofertasClient.atualizarStatusPorLote(id, new StatusOfertaRequestDTO("ESGOTADA"));
        }

        return new LoteResponseDTO(lote);
    }

    @Transactional
    public void deleteLoteById(Long id){
        if (!loteRepository.existsById(id)){
            throw new ResourceNotFoundException("Recurso não encontrado. ID: "+ id);
        }
        loteRepository.deleteById(id);
    }

}

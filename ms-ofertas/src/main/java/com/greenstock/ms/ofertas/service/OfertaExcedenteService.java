package com.greenstock.ms.ofertas.service;

import com.greenstock.ms.ofertas.clients.InventarioClient;
import com.greenstock.ms.ofertas.dtos.*;
import com.greenstock.ms.ofertas.entities.OfertaExcedente;
import com.greenstock.ms.ofertas.entities.StatusOferta;
import com.greenstock.ms.ofertas.exceptions.RegraNegocioException;
import com.greenstock.ms.ofertas.exceptions.ResourceNotFoundException;
import com.greenstock.ms.ofertas.repositories.OfertaExcedenteRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfertaExcedenteService {

    @Autowired
    private OfertaExcedenteRepository repository;

    @Autowired
    private InventarioClient inventarioClient;

    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> findAllOfertas() {
        List<OfertaExcedente> ofertas = repository.findAll();
        return ofertas.stream().map(OfertaResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public OfertaResponseDTO findOfertaById(Long id){
        OfertaExcedente oferta = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado. ID: " + id)
        );
        return new OfertaResponseDTO(oferta);
    }

    @Transactional
    public OfertaResponseDTO saveOferta(OfertaRequestDTO inputDTO) {
        LoteClientResponseDTO lote = inventarioClient.buscarLote(inputDTO.getLoteId());

        if ("VENCIDO".equals(lote.statusLote()) || "ESGOTADO".equals(lote.statusLote())) {
            throw new RegraNegocioException(
                    "Lote não pode virar oferta. Status atual: " + lote.statusLote());
        }

        OfertaExcedente oferta = new OfertaExcedente();
        mapDtoToOferta(inputDTO, oferta);
        oferta.setStatusOferta(StatusOferta.ATIVA);
        oferta = repository.save(oferta);

        inventarioClient.atualizarStatus(lote.id(), new StatusLoteRequestDTO("EM_OFERTA"));

        return new OfertaResponseDTO(oferta);
    }

    private void mapDtoToOferta(OfertaRequestDTO inputDTO, OfertaExcedente oferta){
        oferta.setLoteId(inputDTO.getLoteId());
        oferta.setProdutoNome(inputDTO.getProdutoNome());
        oferta.setQuantidadeDisponivel(inputDTO.getQuantidadeDisponivel());
        oferta.setPrecoPromocional(inputDTO.getPrecoPromocional());
        oferta.setStatusOferta(inputDTO.getStatusOferta());
    }

    @Transactional
    public OfertaResponseDTO updateOferta(Long id, OfertaRequestDTO inputDTO){
        try {
            OfertaExcedente oferta = repository.getReferenceById(id);

            mapDtoToOferta(inputDTO, oferta);

            oferta = repository.save(oferta);
            return new OfertaResponseDTO(oferta);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
    }

    @Transactional
    public List<OfertaResponseDTO> atualizarStatusPorLote(Long loteId, StatusOfertaRequestDTO dto) {
        List<OfertaExcedente> ofertas = repository.findByLoteId(loteId);
        ofertas.forEach(o -> o.setStatusOferta(dto.getStatusOferta()));
        return repository.saveAll(ofertas).stream().map(OfertaResponseDTO::new).toList();
    }

    @Transactional
    public void deleteOfertaById(Long id){
        if (!repository.existsById(id)){
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
        repository.deleteById(id);
    }


}

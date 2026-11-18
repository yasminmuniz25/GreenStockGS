package com.greenstock.ms.ofertas.controller;


import com.greenstock.ms.ofertas.dtos.OfertaRequestDTO;
import com.greenstock.ms.ofertas.dtos.OfertaResponseDTO;
import com.greenstock.ms.ofertas.dtos.StatusOfertaRequestDTO;
import com.greenstock.ms.ofertas.service.OfertaExcedenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaExcedenteController {
    @Autowired
    private OfertaExcedenteService service;

    @GetMapping
    public ResponseEntity<List<OfertaResponseDTO>> getAllOfertas(){
        List<OfertaResponseDTO> list = service.findAllOfertas();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> getOfertaById(@PathVariable Long id){
        OfertaResponseDTO responseDTO = service.findOfertaById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<OfertaResponseDTO> createOferta(@RequestBody @Valid OfertaRequestDTO inputDTO){
        OfertaResponseDTO responseDTO = service.saveOferta(inputDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(responseDTO.getId())
                .toUri();

        return ResponseEntity.created(uri).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> updateOferta(@PathVariable Long id,
                                                                   @RequestBody @Valid OfertaRequestDTO inputDTO){
        OfertaResponseDTO responseDTO = service.updateOferta(id, inputDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/lote/{loteId}/status")
    public ResponseEntity<List<OfertaResponseDTO>> atualizarStatusPorLote(
            @PathVariable Long loteId,
            @RequestBody @Valid StatusOfertaRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarStatusPorLote(loteId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOferta(@PathVariable Long id){
        service.deleteOfertaById(id);
        return ResponseEntity.noContent().build();
    }
}

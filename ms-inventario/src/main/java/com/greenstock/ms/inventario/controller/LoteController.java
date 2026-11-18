package com.greenstock.ms.inventario.controller;

import com.greenstock.ms.inventario.dtos.LoteRequestDTO;
import com.greenstock.ms.inventario.dtos.LoteResponseDTO;
import com.greenstock.ms.inventario.dtos.StatusLoteRequestDTO;
import com.greenstock.ms.inventario.service.LoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lotes")
public class LoteController {
    @Autowired
    private LoteService service;

    @GetMapping
    public ResponseEntity<List<LoteResponseDTO>> getAllLotes(){
        List<LoteResponseDTO> list = service.findAllLotes();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> getLoteById(@PathVariable Long id){

        LoteResponseDTO responseDTO = service.findLoteById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<LoteResponseDTO> createLote(@RequestBody @Valid LoteRequestDTO inputDTO){

        LoteResponseDTO responseDTO = service.saveLote(inputDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(responseDTO.getId()) // Use responseDTO aqui!
                .toUri();

        return ResponseEntity.created(uri).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoteResponseDTO> updateLote(@PathVariable Long id,
                                                      @RequestBody @Valid LoteRequestDTO inputDTO){
        LoteResponseDTO responseDTO = service.updateLote(id, inputDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LoteResponseDTO> atualizarStatus(@PathVariable Long id,
                                                           @RequestBody @Valid StatusLoteRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarStatus(id, dto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable Long id){

        service.deleteLoteById(id);

        return ResponseEntity.noContent().build();
    }
}

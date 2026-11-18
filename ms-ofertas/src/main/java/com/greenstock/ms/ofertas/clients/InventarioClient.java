package com.greenstock.ms.ofertas.clients;

import com.greenstock.ms.ofertas.dtos.LoteClientResponseDTO;
import com.greenstock.ms.ofertas.dtos.StatusLoteRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-inventario", path = "/api/v1/lotes",
        fallbackFactory = InventarioClientFallbackFactory.class)
public interface InventarioClient {

    @GetMapping("/{id}")
    LoteClientResponseDTO buscarLote(@PathVariable("id") Long id);

    @PutMapping("/{id}/status")
    LoteClientResponseDTO atualizarStatus(@PathVariable("id") Long id,
                                          @RequestBody StatusLoteRequestDTO body);
}
package com.greenstock.ms.inventario.clients;

import com.greenstock.ms.inventario.dtos.StatusOfertaRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-ofertas", path = "/api/v1/ofertas",
        fallbackFactory = OfertasClientFallbackFactory.class)
public interface OfertasClient {

    @PutMapping("/lote/{loteId}/status")
    void atualizarStatusPorLote(@PathVariable("loteId") Long loteId,
                                @RequestBody StatusOfertaRequestDTO body);
}
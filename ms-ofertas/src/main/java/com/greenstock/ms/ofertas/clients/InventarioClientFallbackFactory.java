package com.greenstock.ms.ofertas.clients;

import com.greenstock.ms.ofertas.dtos.LoteClientResponseDTO;
import com.greenstock.ms.ofertas.dtos.StatusLoteRequestDTO;
import com.greenstock.ms.ofertas.exceptions.ResourceNotFoundException;
import com.greenstock.ms.ofertas.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class InventarioClientFallbackFactory implements FallbackFactory<InventarioClient> {

    @Override
    public InventarioClient create(Throwable cause) {
        return new InventarioClient() {
            @Override
            public LoteClientResponseDTO buscarLote(Long id) {
                throw traduzir(cause, id);
            }
            @Override
            public LoteClientResponseDTO atualizarStatus(Long id, StatusLoteRequestDTO body) {
                throw traduzir(cause, id);
            }
        };
    }

    private RuntimeException traduzir(Throwable cause, Long id) {
        if (cause instanceof FeignException.NotFound) {
            return new ResourceNotFoundException("Lote não encontrado. ID: " + id);
        }
        return new ServicoIndisponivelException("ms-inventario indisponível no momento");
    }
}
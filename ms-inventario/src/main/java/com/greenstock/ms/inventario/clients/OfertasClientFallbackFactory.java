package com.greenstock.ms.inventario.clients;

import com.greenstock.ms.inventario.dtos.StatusOfertaRequestDTO;
import com.greenstock.ms.inventario.exceptions.ServicoIndisponivelException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class OfertasClientFallbackFactory implements FallbackFactory<OfertasClient> {

    @Override
    public OfertasClient create(Throwable cause) {
        return new OfertasClient() {
            @Override
            public void atualizarStatusPorLote(Long loteId, StatusOfertaRequestDTO body) {
                throw new ServicoIndisponivelException("ms-ofertas indisponível no momento");
            }
        };
    }
}
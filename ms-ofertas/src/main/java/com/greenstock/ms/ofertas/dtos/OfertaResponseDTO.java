package com.greenstock.ms.ofertas.dtos;


import com.greenstock.ms.ofertas.entities.OfertaExcedente;
import com.greenstock.ms.ofertas.entities.StatusOferta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OfertaResponseDTO {
    private Long id;
    private Long loteId;
    private String produtoNome;
    private Integer quantidadeDisponivel;
    private BigDecimal precoPromocional;
    private StatusOferta statusOferta;

    // Construtor utilitário para converter a Entidade direto em Response DTO
    public OfertaResponseDTO(OfertaExcedente entity) {
        this.id = entity.getId();
        this.loteId = entity.getLoteId();
        this.produtoNome = entity.getProdutoNome();
        this.quantidadeDisponivel = entity.getQuantidadeDisponivel();
        this.precoPromocional = entity.getPrecoPromocional();
        this.statusOferta = entity.getStatusOferta();
    }

}

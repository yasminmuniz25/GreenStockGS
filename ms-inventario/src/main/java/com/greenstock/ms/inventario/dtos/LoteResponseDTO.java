package com.greenstock.ms.inventario.dtos;

import com.greenstock.ms.inventario.entities.Lote;
import com.greenstock.ms.inventario.entities.StatusLote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoteResponseDTO {
    private Long id;
    private String nomeProduto;
    private String codigoDeBarras;
    private Integer quantidade;
    private LocalDate dataValidade;
    private StatusLote statusLote;

    public LoteResponseDTO(Lote entity){
        this.id= entity.getId();
        this.nomeProduto=entity.getNomeProduto();
        this.codigoDeBarras= entity.getCodigoDeBarras();
        this.quantidade= entity.getQuantidade();
        this.dataValidade=entity.getDataValidade();
        this.statusLote=entity.getStatusLote();

    }
}



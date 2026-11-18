package com.greenstock.ms.inventario.dtos;

import com.greenstock.ms.inventario.entities.StatusLote;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LoteRequestDTO {

    @NotBlank(message = "O nome do produto não pode estar em branco")
    private String nomeProduto;

    @NotBlank(message = "O código de barras é obrigatório")
    private String codigoDeBarras;

    @NotNull(message = "A quantidade é obrigatória")
    @PositiveOrZero(message = "A quantidade não pode ser negativa")
    private Integer quantidade;

    @NotNull(message = "A data de validade é obrigatória")
    private LocalDate dataValidade;

    @NotNull(message = "O status do lote é obrigatório")
    private StatusLote statusLote;

}

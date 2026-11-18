package com.greenstock.ms.ofertas.dtos;


import com.greenstock.ms.ofertas.entities.StatusOferta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OfertaRequestDTO {
    @NotNull(message = "O ID do lote de origem é obrigatório")
    private Long loteId;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String produtoNome;

    @NotNull(message = "A quantidade disponível é obrigatória")
    @PositiveOrZero(message = "A quantidade disponível não pode ser negativa")
    private Integer quantidadeDisponivel;

    @NotNull(message = "O preço promocional é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço promocional deve ser maior que zero")
    private BigDecimal precoPromocional;

    @NotNull(message = "O status da oferta é obrigatório")
    private StatusOferta statusOferta;
}

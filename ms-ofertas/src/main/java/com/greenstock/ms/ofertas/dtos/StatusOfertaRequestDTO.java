package com.greenstock.ms.ofertas.dtos;

import com.greenstock.ms.ofertas.entities.StatusOferta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StatusOfertaRequestDTO {
    @NotNull(message = "O status da oferta é obrigatório")
    private StatusOferta statusOferta;
}

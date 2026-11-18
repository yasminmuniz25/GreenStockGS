package com.greenstock.ms.inventario.dtos;

import com.greenstock.ms.inventario.entities.StatusLote;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StatusLoteRequestDTO {
    @NotNull(message = "O status do lote é obrigatório")
    private StatusLote statusLote;
}

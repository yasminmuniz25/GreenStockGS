package com.greenstock.ms.inventario.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "tb_lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo nome do produto não pode estar em branco")
    @Column(nullable = false)
    private String nomeProduto;

    @NotBlank(message = "O código de barras é obrigatório")
    @Column(nullable = false, unique = true)
    private String codigoDeBarras;

    @NotNull(message = "O campo quantidade é obrigatória")
    @PositiveOrZero(message = "A quantidade não pode ser negativa")
    private Integer quantidade;

    @NotNull(message = "O campo data de validade é obrigatória")
    private LocalDate dataValidade;

    @NotNull(message = "O status do lote é obrigatório")
    @Enumerated(EnumType.STRING)
    private StatusLote statusLote;


}

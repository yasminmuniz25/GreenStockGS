package com.greenstock.ms.ofertas.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@EqualsAndHashCode(of = "id")
@Table(name = "tb_ofertas")
public class OfertaExcedente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O ID do lote de origem é obrigatório")
    @Column(nullable = false)
    private Long loteId;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Column(nullable = false)
    private String produtoNome;

    @NotNull(message = "A quantidade disponível é obrigatória")
    @PositiveOrZero(message = "A quantidade disponível não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    @NotNull(message = "O preço promocional é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço promocional deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPromocional;


    @NotNull(message = "O status da oferta é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOferta statusOferta;


}

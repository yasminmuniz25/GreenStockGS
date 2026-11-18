package com.greenstock.ms.ofertas.dtos;

import java.time.LocalDate;

public record LoteClientResponseDTO(Long id, String nomeProduto, Integer quantidade,
                                    LocalDate dataValidade, String statusLote) {}
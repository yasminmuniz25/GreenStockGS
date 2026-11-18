package com.greenstock.ms.ofertas.repositories;

import com.greenstock.ms.ofertas.entities.OfertaExcedente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfertaExcedenteRepository extends JpaRepository<OfertaExcedente, Long> {
    List<OfertaExcedente> findByLoteId(Long loteId);
}

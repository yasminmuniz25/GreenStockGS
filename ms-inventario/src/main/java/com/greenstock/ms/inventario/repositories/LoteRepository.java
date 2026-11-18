package com.greenstock.ms.inventario.repositories;

import com.greenstock.ms.inventario.entities.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {
}

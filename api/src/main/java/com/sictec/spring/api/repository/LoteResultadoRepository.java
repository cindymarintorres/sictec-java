package com.sictec.spring.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sictec.spring.api.model.LoteResultado;

public interface LoteResultadoRepository extends JpaRepository<LoteResultado, Long> {
    List<LoteResultado> findByLoteId(String loteId);
    Optional<LoteResultado> findByLoteIdAndRuc(String loteId, String ruc);

}

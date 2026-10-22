package com.sictec.spring.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sictec.spring.api.model.Lote;
//import org.springframework.stereotype.Repository;

public interface LoteRepository extends JpaRepository<Lote, String> {
}

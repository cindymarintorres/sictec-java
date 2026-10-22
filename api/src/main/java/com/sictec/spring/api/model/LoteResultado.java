package com.sictec.spring.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "lote_resultados", indexes = {
        @Index(name = "idx_lote_ruc", columnList = "loteId, ruc")
})
public class LoteResultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String loteId;
    @Column(nullable = false)
    private String ruc;
    @Column(length = 1000)
    private String actividadEconomica;
    private String categoria;

    public LoteResultado() {
    }

    public LoteResultado(String loteId, String ruc, String actividadEconomica, String categoria) {
        this.loteId = loteId;
        this.ruc = ruc;
        this.actividadEconomica = actividadEconomica;
        this.categoria = categoria;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public String getLoteId() {
        return loteId;
    }

    public void setLoteId(String loteId) {
        this.loteId = loteId;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getActividadEconomica() {
        return actividadEconomica;
    }

    public void setActividadEconomica(String actividadEconomica) {
        this.actividadEconomica = actividadEconomica;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

}

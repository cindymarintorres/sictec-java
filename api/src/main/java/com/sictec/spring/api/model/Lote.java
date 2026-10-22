package com.sictec.spring.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    private String id; // UUID
    @Column(nullable = false)
    private String nombreArchivoOriginal;
    private String rutaArchivoAlmacenado;
    private int totalFilas;
    private int totalJobs; // RUCs únicos a procesar
    private int processedJobs;
    private int failedJobs;
    private boolean finished;
    private boolean cancelled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Lote() {
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreArchivoOriginal() {
        return nombreArchivoOriginal;
    }

    public void setNombreArchivoOriginal(String nombreArchivoOriginal) {
        this.nombreArchivoOriginal = nombreArchivoOriginal;
    }

    public String getRutaArchivoAlmacenado() {
        return rutaArchivoAlmacenado;
    }

    public void setRutaArchivoAlmacenado(String rutaArchivoAlmacenado) {
        this.rutaArchivoAlmacenado = rutaArchivoAlmacenado;
    }

    public int getTotalFilas() {
        return totalFilas;
    }

    public void setTotalFilas(int totalFilas) {
        this.totalFilas = totalFilas;
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
        this.totalJobs = totalJobs;
    }

    public int getProcessedJobs() {
        return processedJobs;
    }

    public void setProcessedJobs(int processedJobs) {
        this.processedJobs = processedJobs;
    }

    public int getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(int failedJobs) {
        this.failedJobs = failedJobs;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

package com.sictec.spring.api.controller;

import com.sictec.spring.api.dto.EstadoLoteResponseDto;
import com.sictec.spring.api.dto.SubirLoteResponseDto;
import com.sictec.spring.api.excel.FacturasExcelReader;
import com.sictec.spring.api.service.LoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;
    private final FacturasExcelReader excelReader;

    public LoteController(LoteService loteService, FacturasExcelReader excelReader) {
        this.loteService = loteService;
        this.excelReader = excelReader;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> subirLote(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El archivo es requerido"));
        }
        try {
            // 1. Guardar lote y extraer RUCs
            SubirLoteResponseDto respuesta = loteService.crearLote(archivo);
            // 2. Extraer los RUCs únicos y disparar el procesamiento asíncrono
            var resultadoLectura = excelReader.extraerRucs(archivo.getInputStream());
            loteService.procesarLoteAsync(respuesta.batchId(), resultadoLectura.rucsUnicos());
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al procesar el archivo: " + e.getMessage()));
        }
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<?> obtenerEstado(@PathVariable String batchId) {
        try {
            EstadoLoteResponseDto estado = loteService.obtenerEstadoLote(batchId);
            return ResponseEntity.ok(estado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{batchId}/cancelar")
    public ResponseEntity<?> cancelarLote(@PathVariable String batchId) {
        try {
            loteService.cancelarLote(batchId);
            return ResponseEntity.ok(Map.of("message", "Lote cancelado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }
}
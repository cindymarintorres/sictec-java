package com.sictec.spring.api.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sictec.spring.api.dto.EstadoLoteResponseDto;
import com.sictec.spring.api.dto.SubirLoteResponseDto;
import com.sictec.spring.api.service.LoteService;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> subirLote(@RequestParam("archivo") MultipartFile archivo) throws Exception {
        if (archivo.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "El archivo es requerido"));
        }
        SubirLoteResponseDto respuesta = loteService.iniciarLote(archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<?> obtenerEstado(@PathVariable String batchId) {
        EstadoLoteResponseDto estado = loteService.obtenerEstadoLote(batchId);
        return ResponseEntity.ok(estado);
    }

    @PostMapping("/{batchId}/cancelar")
    public ResponseEntity<?> cancelarLote(@PathVariable String batchId) {
        loteService.cancelarLote(batchId);
        return ResponseEntity.ok(java.util.Map.of("message", "Lote cancelado"));
    }

    @GetMapping("/{batchId}/descargar")
    public ResponseEntity<?> descargarLote(@PathVariable String batchId) throws Exception {
        var archivoDescarga = loteService.descargarLote(batchId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivoDescarga.nombreArchivo() + "\"")
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(archivoDescarga.contenido());
    }
}

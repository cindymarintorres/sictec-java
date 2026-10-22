package com.sictec.spring.api.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sictec.spring.api.dto.SriContribuyenteDto;
import com.sictec.spring.api.service.SriConsultaService;

@RestController
@RequestMapping("/api/sri")
public class SriController {

    private final SriConsultaService sriService;

    public SriController(SriConsultaService sriService) {
        this.sriService = sriService;
    }

    @GetMapping("/validar/{ruc}")
    public ResponseEntity<?> validar(@PathVariable String ruc) {
        boolean esValido = sriService.validar(ruc);
        if (!esValido) {
            return ResponseEntity.status(404).body(Map.of("error", "RUC no válido o no encontrado"));
        }
        return ResponseEntity.ok(Map.of("success", "RUC válido"));
    }

    @GetMapping("/consultar/{ruc}")
    public ResponseEntity<?> consultar(@PathVariable String ruc) {
        SriContribuyenteDto datos = sriService.consultar(ruc);
        if (datos == null) {
            return ResponseEntity.status(404).body(Map.of("error", "RUC no encontrado o el SRI no respondió"));
        }
        return ResponseEntity.ok(datos);
    }
}

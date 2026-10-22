package com.sictec.spring.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SriContribuyenteDto(String numeroRuc,
        String razonSocial,
        String actividadEconomicaPrincipal) {
}

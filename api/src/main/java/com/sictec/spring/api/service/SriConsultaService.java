package com.sictec.spring.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.util.List;

import com.sictec.spring.api.dto.SriContribuyenteDto;

@Service
public class SriConsultaService {

    private static final Logger log = LoggerFactory.getLogger(SriConsultaService.class);
    private final RestClient restClient;
    private final String baseUrl;

    public SriConsultaService(@Value("${sri.api.url}") String baseUrl) {
        this.baseUrl = baseUrl;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Valida si un RUC existe y es válido en el catastro del SRI
     */
    public boolean validar(String ruc) {
        try {
            Boolean existe = this.restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/existePorNumeroRuc")
                            .queryParam("numeroRuc", ruc)
                            .build())
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(existe);
        } catch (Exception e) {
            log.warn("Error validando RUC {} en el SRI: {}", ruc, e.getMessage());
            return false;
        }
    }

    /**
     * Consulta los datos del contribuyente (incluyendo actividadEconomicaPrincipal)
     */
    public SriContribuyenteDto consultar(String ruc) {
        try {
            List<SriContribuyenteDto> respuesta = this.restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/obtenerPorNumerosRuc")
                            .queryParam("ruc", ruc)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SriContribuyenteDto>>() {
                    });
            if (respuesta != null && !respuesta.isEmpty()) {
                return respuesta.getFirst();
            }
            return null;
        } catch (Exception e) {
            log.warn("Error consultando RUC {} en el SRI: {}", ruc, e.getMessage());
            return null;
        }
    }
}

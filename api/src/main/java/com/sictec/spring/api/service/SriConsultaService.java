package com.sictec.spring.api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sictec.spring.api.dto.SriContribuyenteDto;
import com.sictec.spring.api.exception.SriComunicacionException;

@Service
public class SriConsultaService {

    private static final Logger log = LoggerFactory.getLogger(SriConsultaService.class);

    // RestClient inyectado desde SriClientConfig → fácilmente mockeable en tests
    private final RestClient restClient;

    public SriConsultaService(RestClient sriRestClient) {
        this.restClient = sriRestClient;
    }

    /**
     * Valida si un RUC existe y es válido en el catastro del SRI.
     *
     * @return {@code true} si el SRI confirma que el RUC existe.
     * @throws SriComunicacionException si la llamada HTTP falla (timeout, red, etc.).
     *         Esto permite distinguir "RUC inválido" de "no se pudo consultar".
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
            log.warn("Error de comunicación con el SRI al validar RUC {}: {}", ruc, e.getMessage());
            throw new SriComunicacionException(ruc, e);
        }
    }

    /**
     * Consulta los datos del contribuyente (incluyendo actividadEconomicaPrincipal).
     *
     * @return El DTO del contribuyente, o {@code null} si el SRI no devuelve resultados.
     * @throws SriComunicacionException si la llamada HTTP falla.
     */
    public SriContribuyenteDto consultar(String ruc) {
        try {
            List<SriContribuyenteDto> respuesta = this.restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/obtenerPorNumerosRuc")
                            .queryParam("ruc", ruc)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SriContribuyenteDto>>() {});
            if (respuesta != null && !respuesta.isEmpty()) {
                return respuesta.getFirst();
            }
            return null;
        } catch (Exception e) {
            log.warn("Error de comunicación con el SRI al consultar RUC {}: {}", ruc, e.getMessage());
            throw new SriComunicacionException(ruc, e);
        }
    }
}

package com.sictec.spring.api.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP para la API del SRI.
 *
 * <p>Exponer el {@link RestClient} como un {@code @Bean} en lugar de construirlo
 * dentro del servicio permite:</p>
 * <ul>
 *   <li>Inyectarlo como dependencia → testeable con mocks sin levantar un servidor real.</li>
 *   <li>Centralizar timeouts y configuración HTTP en un solo lugar.</li>
 * </ul>
 */
@Configuration
public class SriClientConfig {

    @Bean
    public RestClient sriRestClient(@Value("${sri.api.url}") String baseUrl) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}


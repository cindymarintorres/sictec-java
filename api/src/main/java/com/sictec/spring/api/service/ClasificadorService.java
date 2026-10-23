package com.sictec.spring.api.service;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class ClasificadorService {
    private static final Logger log = LoggerFactory.getLogger(ClasificadorService.class);
    private static final String SIN_CLASIFICAR = "Sin clasificar";
    private Map<String, String> descripcionACodigo = Collections.emptyMap();
    private Map<String, String> prefijoACategoria = Collections.emptyMap();

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 1. Cargar mapa de descripciones oficiales -> código CIIU+SRI
            ClassPathResource descResource = new ClassPathResource("data/descripcion_actividad.json");
            try (InputStream is = descResource.getInputStream()) {
                descripcionACodigo = mapper.readValue(is, new TypeReference<Map<String, String>>() {
                });
            }
            // 2. Cargar mapa de prefijos -> categoría, ordenado del prefijo MÁS LARGO al
            // MÁS CORTO
            ClassPathResource catResource = new ClassPathResource("data/categorias_por_codigo.json");
            try (InputStream is = catResource.getInputStream()) {
                Map<String, String> rawCategorias = mapper.readValue(is, new TypeReference<Map<String, String>>() {
                });

                prefijoACategoria = new LinkedHashMap<>();
                rawCategorias.entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()))
                        .forEach(entry -> prefijoACategoria.put(entry.getKey(), entry.getValue()));
            }
            
            log.info("Catálogo cargado: {} descripciones y {} reglas de categorías",
                    descripcionACodigo.size(), prefijoACategoria.size());
        } catch (Exception e) {
            log.error("Error al cargar los catálogos de clasificación: {}", e.getMessage(), e);
        }
    }

    /**
     * Clasifica una actividad económica devuelta por el SRI a su categoría de
     * gasto.
     */
    public String clasificar(String actividadEconomica) {
        if (actividadEconomica == null || actividadEconomica.isBlank()) {
            return SIN_CLASIFICAR;
        }
        String codigo = obtenerCodigo(actividadEconomica);
        if (codigo == null) {
            return SIN_CLASIFICAR;
        }
        return clasificarPorCodigo(codigo);
    }

    private String obtenerCodigo(String actividadEconomica) {
        String normalizada = normalizar(actividadEconomica);
        return descripcionACodigo.get(normalizada);
    }

    private String clasificarPorCodigo(String codigo) {
        for (Map.Entry<String, String> entry : prefijoACategoria.entrySet()) {
            if (codigo.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return SIN_CLASIFICAR;
    }

    /**
     * Normaliza el texto: elimina tildes/acentos (ASCII), convierte a minúsculas y
     * colapsa espacios.
     */
    private String normalizar(String texto) {
        if (texto == null)
            return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalizado = normalizado.replaceAll("\\p{M}", "");
        normalizado = normalizado.toLowerCase();
        return normalizado.replaceAll("\\s+", " ").trim();
    }
}

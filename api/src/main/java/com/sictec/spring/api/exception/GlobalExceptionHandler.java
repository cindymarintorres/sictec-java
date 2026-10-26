package com.sictec.spring.api.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para todos los controladores REST.
 *
 * <p>Centraliza la traducción de excepciones del dominio a respuestas HTTP,
 * eliminando bloques {@code try-catch} repetitivos en cada endpoint.</p>
 *
 * <ul>
 *   <li>{@link IllegalArgumentException}  → {@code 422 Unprocessable Content}</li>
 *   <li>{@link IllegalStateException}     → {@code 409 Conflict}</li>
 *   <li>{@link SriComunicacionException}  → {@code 502 Bad Gateway}</li>
 *   <li>{@link Exception} (genérico)      → {@code 500 Internal Server Error}</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Argumentos inválidos o recursos no encontrados.
     * Se usa {@code 422 Unprocessable Content} porque el cliente envió datos
     * que no pudieron procesarse (ej: archivo sin columna RUC_EMISOR).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Solicitud no válida: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Estado del recurso incompatible con la operación solicitada
     * (ej: intentar cancelar un lote ya finalizado).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        log.warn("Conflicto de estado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Error de comunicación con la API externa del SRI.
     * {@code 502 Bad Gateway}: el servidor actuó como gateway y recibió
     * una respuesta inválida del servicio upstream.
     */
    @ExceptionHandler(SriComunicacionException.class)
    public ResponseEntity<Map<String, String>> handleSriComunicacion(SriComunicacionException ex) {
        log.error("Error de comunicación con el SRI: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Cualquier excepción no controlada: se registra el stack trace completo
     * y se devuelve un mensaje genérico al cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Error inesperado en el servidor", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error interno del servidor: " + ex.getMessage()));
    }
}

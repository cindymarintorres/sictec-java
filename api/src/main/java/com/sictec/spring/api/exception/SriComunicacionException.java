package com.sictec.spring.api.exception;

/**
 * Excepción lanzada cuando la comunicación con la API del SRI falla
 * (timeout, error de red, respuesta inesperada, etc.).
 *
 * <p>Permite distinguir claramente entre dos casos distintos:</p>
 * <ul>
 *   <li><b>El SRI respondió "RUC no existe"</b> → {@code validar()} devuelve {@code false}.</li>
 *   <li><b>No se pudo contactar al SRI</b> → se lanza {@code SriComunicacionException}.</li>
 * </ul>
 */
public class SriComunicacionException extends RuntimeException {

    public SriComunicacionException(String ruc, Throwable cause) {
        super("No se pudo contactar al SRI para el RUC " + ruc + ": " + cause.getMessage(), cause);
    }
}


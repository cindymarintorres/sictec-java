package com.sictec.spring.api.config;

import java.awt.Desktop;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Abre automáticamente la interfaz web en el navegador del usuario
 * una vez que Spring Boot y Tomcat embebido están listos.
 */
@Component
public class BrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    @EventListener(ApplicationReadyEvent.class)
    public void launchBrowser() {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                // En Windows, FileProtocolHandler abre el navegador predeterminado (Edge, Chrome, etc.)
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", "http://localhost:8080").start();
                log.info("Interfaz abierta automáticamente en http://localhost:8080");
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080"));
                log.info("Navegador lanzado en http://localhost:8080");
            }
        } catch (Exception e) {
            log.info("No se pudo abrir el navegador automáticamente: {}", e.getMessage());
        }
    }
}


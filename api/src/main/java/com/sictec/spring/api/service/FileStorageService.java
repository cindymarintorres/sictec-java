package com.sictec.spring.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio responsable exclusivamente del almacenamiento y recuperación
 * de archivos en el sistema de ficheros local.
 *
 * <p>Usa el directorio del usuario ({@code ${user.home}/.sictec-java/uploads}) para garantizar
 * permisos de escritura tanto en Windows (sin admin) como en Linux y macOS.</p>
 *
 * <p>Si en el futuro el almacenamiento migra a S3 u otro proveedor,
 * solo se modifica esta clase sin tocar la lógica de negocio.</p>
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    // Ruta en el directorio del usuario — siempre escribible sin permisos de admin
    private static final Path STORAGE_DIR = Paths.get(System.getProperty("user.home"), ".sictec-java", "uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(STORAGE_DIR);
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de subidas: {}", e.getMessage());
        }
    }

    /**
     * Guarda un {@link MultipartFile} en el directorio de almacenamiento
     * usando {@code {batchId}_{nombreOriginal}} como nombre de fichero.
     *
     * @param archivo  Archivo recibido en el endpoint.
     * @param batchId  Identificador único del lote, usado como prefijo.
     * @return Ruta absoluta donde quedó almacenado el archivo.
     * @throws IOException Si ocurre un error al escribir en disco.
     */
    public Path guardar(MultipartFile archivo, String batchId) throws IOException {
        Path destino = STORAGE_DIR.resolve(batchId + "_" + archivo.getOriginalFilename());
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toAbsolutePath();
    }

    /**
     * Extrae el nombre base (sin extensión) del nombre de archivo original.
     *
     * @param nombreOriginal Nombre completo del archivo (ej: {@code "facturas.xlsx"}).
     * @return Nombre sin extensión (ej: {@code "facturas"}).
     */
    public String nombreBase(String nombreOriginal) {
        return nombreOriginal.replaceFirst("[.][^.]+$", "");
    }

    /**
     * Determina la extensión de un archivo.
     *
     * @param nombreOriginal Nombre completo del archivo.
     * @return {@code ".xls"} si el archivo es de formato antiguo, {@code ".xlsx"} en caso contrario.
     */
    public String extension(String nombreOriginal) {
        return nombreOriginal.endsWith(".xls") ? ".xls" : ".xlsx";
    }
}


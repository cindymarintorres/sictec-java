package com.sictec.spring.api.service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sictec.spring.api.dto.EstadoLoteResponseDto;
import com.sictec.spring.api.dto.SriContribuyenteDto;
import com.sictec.spring.api.dto.SubirLoteResponseDto;
import com.sictec.spring.api.excel.FacturasExcelReader;
import com.sictec.spring.api.excel.FacturasExcelWriter;
import com.sictec.spring.api.model.Lote;
import com.sictec.spring.api.model.LoteResultado;
import com.sictec.spring.api.repository.LoteRepository;
import com.sictec.spring.api.repository.LoteResultadoRepository;
import com.sictec.spring.api.exception.SriComunicacionException;

@Service
public class LoteService {

    private static final Logger log = LoggerFactory.getLogger(LoteService.class);
    private final LoteRepository loteRepository;
    private final LoteResultadoRepository loteResultadoRepository;
    private final FacturasExcelReader excelReader;
    private final SriConsultaService sriService;
    private final FacturasExcelWriter excelWriter;
    private final ClasificadorService clasificadorService;
    private final FileStorageService fileStorageService;

    public LoteService(LoteRepository loteRepository,
            LoteResultadoRepository loteResultadoRepository,
            FacturasExcelReader excelReader,
            FacturasExcelWriter excelWriter,
            SriConsultaService sriService,
            ClasificadorService clasificadorService,
            FileStorageService fileStorageService) {
        this.loteRepository = loteRepository;
        this.loteResultadoRepository = loteResultadoRepository;
        this.excelReader = excelReader;
        this.excelWriter = excelWriter;
        this.sriService = sriService;
        this.clasificadorService = clasificadorService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Punto de entrada unificado: lee el Excel UNA sola vez, guarda el archivo
     * en disco y dispara el procesamiento asíncrono en un único método.
     * El controlador solo necesita llamar a este método.
     */
    public SubirLoteResponseDto iniciarLote(MultipartFile archivo) throws Exception {
        SubirLoteResponseDto respuesta = crearLote(archivo);
        // Los RUCs únicos ya están en la BD; los releemos del lote para el async
        Lote lote = loteRepository.findById(respuesta.batchId()).orElseThrow();
        var resultadoLectura = excelReader.extraerRucs(
                java.nio.file.Files.newInputStream(java.nio.file.Paths.get(lote.getRutaArchivoAlmacenado())));
        procesarLoteAsync(respuesta.batchId(), resultadoLectura.rucsUnicos());
        return respuesta;
    }

    /**
     * 1. Lee los RUCs del archivo Excel
     * 2. Guarda el archivo original en disco mediante FileStorageService
     * 3. Registra el Lote en la BD H2
     */
    public SubirLoteResponseDto crearLote(MultipartFile archivo) throws Exception {
        var resultadoLectura = excelReader.extraerRucs(archivo.getInputStream());
        List<String> rucsTotales = resultadoLectura.rucsTotales();
        List<String> rucsUnicos = resultadoLectura.rucsUnicos();
        if (rucsUnicos.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron RUC válidos en la columna RUC_EMISOR");
        }
        String batchId = UUID.randomUUID().toString();
        // Delegar el guardado en disco a FileStorageService
        Path destino = fileStorageService.guardar(archivo, batchId);
        // Crear la entidad Lote en H2
        Lote lote = new Lote();
        lote.setId(batchId);
        lote.setNombreArchivoOriginal(archivo.getOriginalFilename());
        lote.setRutaArchivoAlmacenado(destino.toString());
        lote.setTotalFilas(rucsTotales.size());
        lote.setTotalJobs(rucsUnicos.size());
        lote.setProcessedJobs(0);
        lote.setFailedJobs(0);
        lote.setFinished(false);
        lote.setCancelled(false);
        loteRepository.save(lote);
        return new SubirLoteResponseDto(
                batchId,
                rucsTotales.size(),
                rucsUnicos.size(),
                rucsTotales.size() - rucsUnicos.size());
    }

    /**
     * Procesa los RUCs del lote en segundo plano (@Async)
     */
    @Async("taskExecutor")
    public void procesarLoteAsync(String batchId, List<String> rucsUnicos) {
        log.info("Iniciando procesamiento asíncrono para el lote: {}", batchId);
        for (String ruc : rucsUnicos) {
            // Verificar si el usuario canceló el lote
            Lote loteActual = loteRepository.findById(batchId).orElse(null);
            if (loteActual == null || loteActual.isCancelled()) {
                log.info("Lote {} cancelado o no encontrado. Deteniendo procesamiento.", batchId);
                return;
            }
            try {
                // 1. Validar en el SRI
                boolean valido = sriService.validar(ruc);
                String actividad = null;
                String categoria = "RUC inválido";
                if (valido) {
                    SriContribuyenteDto datos = sriService.consultar(ruc);
                    actividad = (datos != null) ? datos.actividadEconomicaPrincipal() : null;
                    categoria = clasificadorService.clasificar(actividad);
                }
                // Guardar resultado del RUC en H2
                LoteResultado res = new LoteResultado(batchId, ruc, actividad, categoria);
                loteResultadoRepository.save(res);
                loteActual.setProcessedJobs(loteActual.getProcessedJobs() + 1);
            } catch (SriComunicacionException e) {
                // El SRI no respondió: marcamos como fallido pero con categoría descriptiva
                log.error("Error de comunicación con el SRI para RUC {}: {}", ruc, e.getMessage());
                LoteResultado res = new LoteResultado(batchId, ruc, null, "Error de comunicación SRI");
                loteResultadoRepository.save(res);
                loteActual.setFailedJobs(loteActual.getFailedJobs() + 1);
            } catch (Exception e) {
                log.error("Error inesperado procesando RUC {}: {}", ruc, e.getMessage());
                loteActual.setFailedJobs(loteActual.getFailedJobs() + 1);
            }
            loteRepository.save(loteActual);
        }
        // Marcar como finalizado
        loteRepository.findById(batchId).ifPresent(lote -> {
            lote.setFinished(true);
            loteRepository.save(lote);
            log.info("Lote {} completado con éxito.", batchId);
        });
    }

    public EstadoLoteResponseDto obtenerEstadoLote(String batchId) {
        Lote lote = loteRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        int pendingJobs = Math.max(0, lote.getTotalJobs() - lote.getProcessedJobs() - lote.getFailedJobs());
        return new EstadoLoteResponseDto(
                lote.getTotalJobs(),
                pendingJobs,
                lote.getProcessedJobs(),
                lote.getFailedJobs(),
                lote.isFinished(),
                lote.isCancelled());
    }

    public void cancelarLote(String batchId) {
        Lote lote = loteRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));
        if (lote.isFinished()) {
            throw new IllegalStateException("El lote ya terminó, no se puede cancelar");
        }
        lote.setCancelled(true);
        loteRepository.save(lote);
    }

    public record ArchivoDescarga(String nombreArchivo, byte[] contenido) {
    }

    public ArchivoDescarga descargarLote(String batchId) throws Exception {
        Lote lote = loteRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado"));

        if (!lote.isFinished() && !lote.isCancelled()) {
            throw new IllegalStateException("El lote aún no ha terminado de procesarse");
        }

        // Obtener todos los resultados clasificados del lote
        List<LoteResultado> resultados = loteResultadoRepository.findByLoteId(batchId);
        Map<String, LoteResultado> mapaResultados = new HashMap<>();

        for (LoteResultado res : resultados) {
            mapaResultados.putIfAbsent(res.getRuc(), res);
        }

        byte[] contenido = excelWriter.generarExcelClasificado(lote.getRutaArchivoAlmacenado(), mapaResultados);

        String nombreBase = fileStorageService.nombreBase(lote.getNombreArchivoOriginal());
        String extension = fileStorageService.extension(lote.getNombreArchivoOriginal());
        String nombreDescarga = nombreBase + "_clasificado" + extension;

        return new ArchivoDescarga(nombreDescarga, contenido);
    }
}

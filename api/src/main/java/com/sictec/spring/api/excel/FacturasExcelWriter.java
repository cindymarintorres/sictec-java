package com.sictec.spring.api.excel;

import com.sictec.spring.api.model.LoteResultado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;

@Component
public class FacturasExcelWriter {

    // Color exacto del header según el archivo original (#4472C4) y filas alternas
    private static final String COLOR_HEADER_FONDO = "4472C4";
    private static final String COLOR_HEADER_TEXTO = "FFFFFF";
    private static final String COLOR_FILA_PAR    = "DEEBF6";
    private static final String COLOR_FILA_IMPAR  = "FFFFFF";
    private static final String COLOR_TEXTO_DATOS  = "000000";

    private static final DateTimeFormatter[] DATE_TIME_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("M/d/yy HH:mm"),
            DateTimeFormatter.ofPattern("M/d/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("M/d/yy"),
            DateTimeFormatter.ofPattern("M/d/yyyy")
    };

    private XSSFCellStyle crearEstilo(XSSFWorkbook wb, String hexFondo, String hexTexto, boolean bold) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(ExcelUtils.hexColor(hexFondo));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(bold);
        font.setColor(ExcelUtils.hexColor(hexTexto));
        style.setFont(font);
        return style;
    }

    public byte[] generarExcelClasificado(String rutaOriginal, Map<String, LoteResultado> resultadosPorRuc)
            throws Exception {

        try (InputStream is = new FileInputStream(rutaOriginal);
             Workbook workbook = WorkbookFactory.create(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalStateException("El archivo original no tiene fila de cabecera");
            }

            // 1. Identificar columnas clave
            int totalColumnas = headerRow.getLastCellNum();
            int indiceRuc = ExcelUtils.buscarIndiceColumna(headerRow, ExcelUtils.COL_RUC_EMISOR);
            int indiceFechaAut = ExcelUtils.buscarIndiceColumna(headerRow, ExcelUtils.COL_FECHA_AUTORIZACION);
            int indiceFechaEmision = ExcelUtils.buscarIndiceColumna(headerRow, ExcelUtils.COL_FECHA_EMISION);

            int colActividad = totalColumnas;
            int colCategoria = totalColumnas + 1;

            // 2. Estilos reutilizables creados una sola vez por Workbook
            XSSFCellStyle estiloHeader = null;
            XSSFCellStyle estiloFilaPar = null;
            XSSFCellStyle estiloFilaImpar = null;
            XSSFCellStyle estiloFechaHoraPar = null;
            XSSFCellStyle estiloFechaHoraImpar = null;
            XSSFCellStyle estiloFechaPar = null;
            XSSFCellStyle estiloFechaImpar = null;

            if (workbook instanceof XSSFWorkbook xssfWb) {
                CreationHelper helper = xssfWb.getCreationHelper();

                // Cabeceras nuevas
                estiloHeader = crearEstilo(xssfWb, COLOR_HEADER_FONDO, COLOR_HEADER_TEXTO, true);

                // Filas datos generales
                estiloFilaPar = crearEstilo(xssfWb, COLOR_FILA_PAR, COLOR_TEXTO_DATOS, false);
                estiloFilaImpar = crearEstilo(xssfWb, COLOR_FILA_IMPAR, COLOR_TEXTO_DATOS, false);

                // Fecha + hora explícito dd/MM/yyyy HH:mm
                short fmtFechaHora = helper.createDataFormat().getFormat("dd/MM/yyyy HH:mm");
                estiloFechaHoraPar = crearEstilo(xssfWb, COLOR_FILA_PAR, COLOR_TEXTO_DATOS, false);
                estiloFechaHoraPar.setDataFormat(fmtFechaHora);

                estiloFechaHoraImpar = crearEstilo(xssfWb, COLOR_FILA_IMPAR, COLOR_TEXTO_DATOS, false);
                estiloFechaHoraImpar.setDataFormat(fmtFechaHora);

                // Fecha solo día explícito dd/MM/yyyy
                short fmtFecha = helper.createDataFormat().getFormat("dd/MM/yyyy");
                estiloFechaPar = crearEstilo(xssfWb, COLOR_FILA_PAR, COLOR_TEXTO_DATOS, false);
                estiloFechaPar.setDataFormat(fmtFecha);

                estiloFechaImpar = crearEstilo(xssfWb, COLOR_FILA_IMPAR, COLOR_TEXTO_DATOS, false);
                estiloFechaImpar.setDataFormat(fmtFecha);
            }

            // 3. Cabeceras nuevas (las cabeceras existentes NO se tocan para preservar sus estilos y colores originales)
            Cell cActividad = headerRow.createCell(colActividad);
            cActividad.setCellValue(ExcelUtils.COL_ACTIVIDAD_ECONOMICA);
            if (estiloHeader != null) cActividad.setCellStyle(estiloHeader);

            Cell cCategoria = headerRow.createCell(colCategoria);
            cCategoria.setCellValue(ExcelUtils.COL_CATEGORIA);
            if (estiloHeader != null) cCategoria.setCellStyle(estiloHeader);

            // 4. Rellenar datos
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // Leer RUC de la fila
                String ruc = "";
                if (indiceRuc != -1) {
                    Cell c = row.getCell(indiceRuc);
                    if (c != null) ruc = ExcelUtils.extraerRuc(c);
                }

                // Leer resultado del RUC
                LoteResultado res = resultadosPorRuc.get(ruc);
                String actividad = (res != null && res.getActividadEconomica() != null)
                        ? res.getActividadEconomica() : "No encontrado";
                String categoria = (res != null && res.getCategoria() != null)
                        ? res.getCategoria() : "No encontrado";

                boolean isPar = ((r + 1) % 2 == 0);

                if (estiloFilaPar != null) {
                    XSSFCellStyle estiloFila = isPar ? estiloFilaPar : estiloFilaImpar;

                    for (int c = 0; c < totalColumnas; c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) {
                            cell = row.createCell(c);
                            cell.setCellStyle(estiloFila);
                            continue;
                        }

                        if (c == indiceFechaAut) {
                            LocalDateTime ldt = extraerLocalDateTime(cell);
                            if (ldt != null) {
                                cell.setCellValue(ldt);
                            }
                            cell.setCellStyle(isPar ? estiloFechaHoraPar : estiloFechaHoraImpar);
                        } else if (c == indiceFechaEmision) {
                            LocalDate ld = extraerLocalDate(cell);
                            if (ld != null) {
                                cell.setCellValue(ld);
                            }
                            cell.setCellStyle(isPar ? estiloFechaPar : estiloFechaImpar);
                        } else if (c == indiceRuc) {
                            // Preservar RUC como texto con los 13 dígitos y cero a la izquierda
                            String rucTexto = ExcelUtils.extraerRuc(cell);
                            cell.setCellValue(rucTexto);
                            cell.setCellStyle(estiloFila);
                        } else {
                            cell.setCellStyle(estiloFila);
                        }
                    }

                    Cell cAct = row.createCell(colActividad);
                    cAct.setCellValue(actividad);
                    cAct.setCellStyle(estiloFila);

                    Cell cCat = row.createCell(colCategoria);
                    cCat.setCellValue(categoria);
                    cCat.setCellStyle(estiloFila);
                } else {
                    // Fallback sin estilos (.xls antiguo)
                    for (int c = 0; c < totalColumnas; c++) {
                        Cell cell = row.getCell(c);
                        if (cell == null) continue;
                        if (c == indiceFechaAut) {
                            LocalDateTime ldt = extraerLocalDateTime(cell);
                            if (ldt != null) cell.setCellValue(ldt);
                        } else if (c == indiceFechaEmision) {
                            LocalDate ld = extraerLocalDate(cell);
                            if (ld != null) cell.setCellValue(ld);
                        } else if (c == indiceRuc) {
                            String rucTexto = ExcelUtils.extraerRuc(cell);
                            cell.setCellValue(rucTexto);
                        }
                    }
                    row.createCell(colActividad).setCellValue(actividad);
                    row.createCell(colCategoria).setCellValue(categoria);
                }
            }

            // 5. Ajustar anchos para evitar desbordes o "#####"
            if (indiceFechaAut != -1) {
                sheet.setColumnWidth(indiceFechaAut, 20 * 256);
            }
            if (indiceFechaEmision != -1) {
                sheet.setColumnWidth(indiceFechaEmision, 14 * 256);
            }
            sheet.autoSizeColumn(colActividad);
            sheet.autoSizeColumn(colCategoria);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private LocalDateTime extraerLocalDateTime(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            try {
                return cell.getLocalDateTimeCellValue();
            } catch (Exception e) {
                Date d = cell.getDateCellValue();
                if (d != null) {
                    return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            }
        }
        String texto = ExcelUtils.FORMATTER.formatCellValue(cell);
        return parseLocalDateTime(texto);
    }

    private LocalDate extraerLocalDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            try {
                LocalDateTime ldt = cell.getLocalDateTimeCellValue();
                if (ldt != null) return ldt.toLocalDate();
            } catch (Exception e) {
                Date d = cell.getDateCellValue();
                if (d != null) {
                    return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
            }
        }
        String texto = ExcelUtils.FORMATTER.formatCellValue(cell);
        return parseLocalDate(texto);
    }

    private LocalDateTime parseLocalDateTime(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String val = texto.trim();
        for (DateTimeFormatter fmt : DATE_TIME_FORMATTERS) {
            try {
                TemporalAccessor ta = fmt.parseBest(val, LocalDateTime::from, LocalDate::from);
                if (ta instanceof LocalDateTime ldt) {
                    return ldt;
                } else if (ta instanceof LocalDate ld) {
                    return ld.atStartOfDay();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private LocalDate parseLocalDate(String texto) {
        LocalDateTime ldt = parseLocalDateTime(texto);
        return ldt != null ? ldt.toLocalDate() : null;
    }
}

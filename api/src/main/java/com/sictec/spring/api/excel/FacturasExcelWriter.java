package com.sictec.spring.api.excel;

import com.sictec.spring.api.model.LoteResultado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Component
public class FacturasExcelWriter {

    // Colores idénticos a la versión PHP/Laravel
    private static final String COLOR_HEADER_FONDO = "5B9BD5";
    private static final String COLOR_HEADER_TEXTO = "FFFFFF";
    private static final String COLOR_FILA_PAR    = "DEEBF6";
    private static final String COLOR_FILA_IMPAR  = "FFFFFF";
    private static final String COLOR_TEXTO_DATOS  = "000000";

    private final DataFormatter formatter = new DataFormatter();

    /**
     * Convierte un color HEX (sin #, ej: "5B9BD5") a XSSFColor.
     * Apache POI internamente usa bytes RGB.
     */
    private XSSFColor hexColor(String hex) {
        byte[] rgb = new byte[]{
            (byte) Integer.parseInt(hex.substring(0, 2), 16),
            (byte) Integer.parseInt(hex.substring(2, 4), 16),
            (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
        return new XSSFColor(rgb, new DefaultIndexedColorMap());
    }

    private XSSFCellStyle crearEstilo(XSSFWorkbook wb, String hexFondo, String hexTexto, boolean bold) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(hexColor(hexFondo));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(bold);
        font.setColor(hexColor(hexTexto));
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

            // 1. Identificar columna RUC_EMISOR (sin tocar nada)
            int indiceRuc = -1;
            int totalColumnas = headerRow.getLastCellNum();
            for (Cell cell : headerRow) {
                if ("RUC_EMISOR".equalsIgnoreCase(formatter.formatCellValue(cell).trim())) {
                    indiceRuc = cell.getColumnIndex();
                    break;
                }
            }

            int colActividad = totalColumnas;
            int colCategoria = totalColumnas + 1;

            // 2. Estilos (solo si el archivo es .xlsx / XSSFWorkbook)
            XSSFCellStyle estiloHeader   = null;
            XSSFCellStyle estiloFilaPar  = null;
            XSSFCellStyle estiloFilaImpar = null;

            if (workbook instanceof XSSFWorkbook xssfWb) {
                estiloHeader    = crearEstilo(xssfWb, COLOR_HEADER_FONDO, COLOR_HEADER_TEXTO, true);
                estiloFilaPar   = crearEstilo(xssfWb, COLOR_FILA_PAR,    COLOR_TEXTO_DATOS,   false);
                estiloFilaImpar = crearEstilo(xssfWb, COLOR_FILA_IMPAR,  COLOR_TEXTO_DATOS,   false);
            }

            // 3. Cabeceras nuevas
            Cell cActividad = headerRow.createCell(colActividad);
            cActividad.setCellValue("ACTIVIDAD_ECONOMICA");
            if (estiloHeader != null) cActividad.setCellStyle(estiloHeader);

            Cell cCategoria = headerRow.createCell(colCategoria);
            cCategoria.setCellValue("CATEGORIA");
            if (estiloHeader != null) cCategoria.setCellStyle(estiloHeader);

            // 4. Rellenar datos (lógica original intacta) + estilo alterno por fila
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // Leer RUC PRIMERO (sin modificar la celda)
                String ruc = "";
                if (indiceRuc != -1) {
                    Cell c = row.getCell(indiceRuc);
                    if (c != null) ruc = formatter.formatCellValue(c).trim();
                }

                // Aplicar color alterno a celdas existentes
                if (estiloFilaPar != null) {
                    XSSFCellStyle estiloFila = ((r + 1) % 2 == 0) ? estiloFilaPar : estiloFilaImpar;
                    for (int c = 0; c < totalColumnas; c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) cell.setCellStyle(estiloFila);
                    }

                    LoteResultado res = resultadosPorRuc.get(ruc);
                    String actividad = (res != null && res.getActividadEconomica() != null)
                            ? res.getActividadEconomica() : "No encontrado";
                    String categoria = (res != null && res.getCategoria() != null)
                            ? res.getCategoria() : "No encontrado";

                    Cell cAct = row.createCell(colActividad);
                    cAct.setCellValue(actividad);
                    cAct.setCellStyle(estiloFila);

                    Cell cCat = row.createCell(colCategoria);
                    cCat.setCellValue(categoria);
                    cCat.setCellStyle(estiloFila);
                } else {
                    // Sin estilos (archivo .xls antiguo): misma lógica original
                    LoteResultado res = resultadosPorRuc.get(ruc);
                    String actividad = (res != null && res.getActividadEconomica() != null)
                            ? res.getActividadEconomica() : "No encontrado";
                    String categoria = (res != null && res.getCategoria() != null)
                            ? res.getCategoria() : "No encontrado";
                    row.createCell(colActividad).setCellValue(actividad);
                    row.createCell(colCategoria).setCellValue(categoria);
                }
            }

            // 5. Autoajustar ancho de las nuevas columnas
            sheet.autoSizeColumn(colActividad);
            sheet.autoSizeColumn(colCategoria);

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

package com.sictec.spring.api.excel;

import com.sictec.spring.api.model.LoteResultado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

@Component
public class FacturasExcelWriter {
    private final DataFormatter formatter = new DataFormatter();

    /**
     * Lee el Excel original, agrega las columnas ACTIVIDAD_ECONOMICA y CATEGORIA
     * con estilos,
     * y devuelve los bytes del archivo listo para descarga.
     */
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

            // 1. Identificar columna RUC_EMISOR
            int indiceRuc = -1;
            int totalColumnas = headerRow.getLastCellNum();
            for (Cell cell : headerRow) {
                String val = formatter.formatCellValue(cell).trim();
                if ("RUC_EMISOR".equalsIgnoreCase(val)) {
                    indiceRuc = cell.getColumnIndex();
                    break;
                }
            }

            // 2. Crear estilo para las nuevas cabeceras (Azul #5B9BD5, texto blanco
            // negrita)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            // Color de fondo azul #5B9BD5
            byte[] rgbAzul = new byte[] { (byte) 0x5B, (byte) 0x9B, (byte) 0xD5 };
            XSSFColor colorFondo = new XSSFColor(rgbAzul, new DefaultIndexedColorMap());
            headerStyle.setFillForegroundColor(colorFondo.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // Agregar cabeceras nuevas al final
            int colActividad = totalColumnas;
            int colCategoria = totalColumnas + 1;
            Cell cellActividad = headerRow.createCell(colActividad);
            cellActividad.setCellValue("ACTIVIDAD_ECONOMICA");
            cellActividad.setCellStyle(headerStyle);
            Cell cellCategoria = headerRow.createCell(colCategoria);
            cellCategoria.setCellValue("CATEGORIA");
            cellCategoria.setCellStyle(headerStyle);
            
            // 3. Rellenar datos en cada fila
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;
                String ruc = "";
                if (indiceRuc != -1) {
                    Cell c = row.getCell(indiceRuc);
                    if (c != null) {
                        ruc = formatter.formatCellValue(c).trim();
                    }
                }
                LoteResultado res = resultadosPorRuc.get(ruc);
                String actividad = (res != null && res.getActividadEconomica() != null)
                        ? res.getActividadEconomica()
                        : "No encontrado";
                String categoria = (res != null && res.getCategoria() != null)
                        ? res.getCategoria()
                        : "No encontrado";
                Cell cAct = row.createCell(colActividad);
                cAct.setCellValue(actividad);
                Cell cCat = row.createCell(colCategoria);
                cCat.setCellValue(categoria);
            }
            workbook.write(out);
            return out.toByteArray();
        }

    }
}

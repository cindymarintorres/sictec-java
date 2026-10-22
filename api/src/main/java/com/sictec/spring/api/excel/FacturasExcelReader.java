package com.sictec.spring.api.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

@Component
public class FacturasExcelReader {

    private final DataFormatter formatter = new DataFormatter();

    public record ResultadoLectura(List<String> rucsTotales, List<String> rucsUnicos) {}

    /**
     * Lee un archivo Excel (.xlsx o .xls) y extrae los RUCs de la columna RUC_EMISOR
     */
    public ResultadoLectura extraerRucs(InputStream inputStream) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return new ResultadoLectura(List.of(), List.of());
            }

            // 1. Buscar la columna RUC_EMISOR en la primera fila (cabeceras)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return new ResultadoLectura(List.of(), List.of());
            }

            int indiceRuc = -1;
            for (Cell cell : headerRow) {
                String headerName = formatter.formatCellValue(cell).trim();
                if ("RUC_EMISOR".equalsIgnoreCase(headerName)) {
                    indiceRuc = cell.getColumnIndex();
                    break;
                }
            }

            if (indiceRuc == -1) {
                return new ResultadoLectura(List.of(), List.of());
            }

            // 2. Extraer los RUCs de las filas de datos
            List<String> rucsTotales = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Cell cell = row.getCell(indiceRuc);
                if (cell == null) continue;

                String ruc = formatter.formatCellValue(cell).trim();
                if (!ruc.isEmpty()) {
                    rucsTotales.add(ruc);
                }
            }

            List<String> rucsUnicos = rucsTotales.stream().distinct().toList();
            return new ResultadoLectura(rucsTotales, rucsUnicos);
        }
    }
}
package com.sictec.spring.api.excel;

import com.sictec.spring.api.model.LoteResultado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FacturasExcelWriterTest {

    @Test
    void testFormatoFechasYRuc(@TempDir Path tempDir) throws Exception {
        File testFile = tempDir.resolve("test_facturas.xlsx").toFile();
        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(testFile)) {
            Sheet sheet = wb.createSheet("Facturas");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("RUC_EMISOR");
            header.createCell(1).setCellValue("RAZON_SOCIAL_EMISOR");
            header.createCell(2).setCellValue("FECHA_AUTORIZACION");
            header.createCell(3).setCellValue("FECHA_EMISION");

            // Fila 1: RUC numérico (12 dígitos sin 0) y fechas como strings estilo US o regional
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(993367439001.0);
            row1.createCell(1).setCellValue("EMPRESA 1");
            row1.createCell(2).setCellValue("7/1/26 21:34");
            row1.createCell(3).setCellValue("7/1/26");

            // Fila 2: RUC ya formateado y fechas numéricas
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("0914953393001");
            row2.createCell(1).setCellValue("EMPRESA 2");
            Cell fAut2 = row2.createCell(2);
            fAut2.setCellValue(LocalDateTime.of(2026, 7, 2, 21, 46));
            Cell fEm2 = row2.createCell(3);
            fEm2.setCellValue(LocalDate.of(2026, 7, 2));

            wb.write(fos);
        }

        FacturasExcelWriter writer = new FacturasExcelWriter();
        Map<String, LoteResultado> resultados = Map.of(
            "0993367439001", new LoteResultado("batch1", "0993367439001", "Venta de repuestos", "Comercio"),
            "0914953393001", new LoteResultado("batch1", "0914953393001", "Servicios técnicos", "Servicios")
        );

        byte[] salida = writer.generarExcelClasificado(testFile.getAbsolutePath(), resultados);

        try (InputStream is = new ByteArrayInputStream(salida);
             Workbook wbSalida = WorkbookFactory.create(is)) {
            Sheet sheet = wbSalida.getSheetAt(0);

            // Validar fila 1
            Row row1 = sheet.getRow(1);
            Cell rucCell1 = row1.getCell(0);
            assertEquals(CellType.STRING, rucCell1.getCellType());
            assertEquals("0993367439001", rucCell1.getStringCellValue());

            Cell fechaAutCell1 = row1.getCell(2);
            assertEquals(CellType.NUMERIC, fechaAutCell1.getCellType());
            assertEquals("dd/MM/yyyy HH:mm", fechaAutCell1.getCellStyle().getDataFormatString());
            assertEquals("01/07/2026 21:34", ExcelUtils.FORMATTER.formatCellValue(fechaAutCell1));

            Cell fechaEmCell1 = row1.getCell(3);
            assertEquals(CellType.NUMERIC, fechaEmCell1.getCellType());
            assertEquals("dd/MM/yyyy", fechaEmCell1.getCellStyle().getDataFormatString());
            assertEquals("01/07/2026", ExcelUtils.FORMATTER.formatCellValue(fechaEmCell1));

            // Validar fila 2
            Row row2 = sheet.getRow(2);
            Cell rucCell2 = row2.getCell(0);
            assertEquals("0914953393001", rucCell2.getStringCellValue());

            Cell fechaAutCell2 = row2.getCell(2);
            assertEquals(CellType.NUMERIC, fechaAutCell2.getCellType());
            assertEquals("dd/MM/yyyy HH:mm", fechaAutCell2.getCellStyle().getDataFormatString());
            assertEquals("02/07/2026 21:46", ExcelUtils.FORMATTER.formatCellValue(fechaAutCell2));

            Cell fechaEmCell2 = row2.getCell(3);
            assertEquals(CellType.NUMERIC, fechaEmCell2.getCellType());
            assertEquals("dd/MM/yyyy", fechaEmCell2.getCellStyle().getDataFormatString());
            assertEquals("02/07/2026", ExcelUtils.FORMATTER.formatCellValue(fechaEmCell2));

            // Validar anchos de columnas de fechas
            assertTrue(sheet.getColumnWidth(2) >= 20 * 256);
            assertTrue(sheet.getColumnWidth(3) >= 14 * 256);

            // Validar cabeceras nuevas con color #4472C4
            Row headerRow = sheet.getRow(0);
            Cell cActividad = headerRow.getCell(4);
            assertEquals("ACTIVIDAD_ECONOMICA", cActividad.getStringCellValue());
            XSSFCellStyle estiloAct = (XSSFCellStyle) cActividad.getCellStyle();
            assertEquals("FF4472C4", estiloAct.getFillForegroundColorColor().getARGBHex());

            Cell cCategoria = headerRow.getCell(5);
            assertEquals("CATEGORIA", cCategoria.getStringCellValue());
            XSSFCellStyle estiloCat = (XSSFCellStyle) cCategoria.getCellStyle();
            assertEquals("FF4472C4", estiloCat.getFillForegroundColorColor().getARGBHex());
        }
    }
}

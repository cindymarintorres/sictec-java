package com.sictec.spring.api.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * Utilidades compartidas para operaciones con archivos Excel.
 * Centraliza el formateador de celdas, las constantes de columnas y
 * helpers reutilizables, eliminando la duplicación entre Reader y Writer.
 */
public final class ExcelUtils {

    // ── Constantes de nombres de columna ────────────────────────────────────
    public static final String COL_RUC_EMISOR        = "RUC_EMISOR";
    public static final String COL_ACTIVIDAD_ECONOMICA = "ACTIVIDAD_ECONOMICA";
    public static final String COL_CATEGORIA          = "CATEGORIA";
    public static final String COL_FECHA_AUTORIZACION = "FECHA_AUTORIZACION";
    public static final String COL_FECHA_EMISION      = "FECHA_EMISION";

    // ── Formateador compartido (hilo-seguro en lectura) ─────────────────────
    public static final DataFormatter FORMATTER = new DataFormatter();

    /** Clase utilitaria: no debe instanciarse. */
    private ExcelUtils() {}

    /**
     * Extrae el RUC de una celda garantizando los 13 dígitos con el cero a la izquierda,
     * incluso si la celda es numérica o está en notación científica.
     */
    public static String extraerRuc(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            long num = Math.round(cell.getNumericCellValue());
            return String.format("%013d", num);
        }
        String texto = FORMATTER.formatCellValue(cell).trim();
        if (texto.contains("E+") || texto.contains("e+")) {
            try {
                double d = Double.parseDouble(texto.replace(",", "."));
                return String.format("%013d", Math.round(d));
            } catch (Exception ignored) {
            }
        }
        if (texto.length() == 12 && texto.chars().allMatch(Character::isDigit)) {
            return "0" + texto;
        }
        return texto;
    }

    /**
     * Busca el índice de la primera columna cuyo encabezado coincida
     * (ignorando mayúsculas y espacios al inicio/fin) con {@code nombreColumna}.
     *
     * @param headerRow    Fila de cabeceras del Excel.
     * @param nombreColumna Nombre exacto de la columna a buscar.
     * @return Índice de la columna, o {@code -1} si no se encuentra.
     */
    public static int buscarIndiceColumna(Row headerRow, String nombreColumna) {
        if (headerRow == null || nombreColumna == null) {
            return -1;
        }
        for (Cell cell : headerRow) {
            String header = FORMATTER.formatCellValue(cell).trim();
            if (nombreColumna.equalsIgnoreCase(header)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    /**
     * Convierte un color hexadecimal de 6 dígitos (sin {@code #}, ej: {@code "5B9BD5"})
     * a un {@link XSSFColor} compatible con Apache POI.
     *
     * @param hex Color en formato hexadecimal de 6 dígitos.
     * @return {@link XSSFColor} con los bytes RGB correspondientes.
     */
    public static XSSFColor hexColor(String hex) {
        byte[] rgb = new byte[]{
            (byte) Integer.parseInt(hex.substring(0, 2), 16),
            (byte) Integer.parseInt(hex.substring(2, 4), 16),
            (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
        return new XSSFColor(rgb, new DefaultIndexedColorMap());
    }
}


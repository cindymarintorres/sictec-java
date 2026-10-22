import { z } from "zod";

const TIPOS_MIME_PERMITIDOS = [
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
  "application/vnd.ms-excel", // .xls
  "text/csv",
];
const EXTENSIONES_PERMITIDAS = [".xlsx", ".xls", ".csv"];
const TAMANO_MAXIMO_BYTES = 10 * 1024 * 1024; // 10MB, ajustable

function tieneFormatoValido(archivo: File): boolean {
  const porMime = TIPOS_MIME_PERMITIDOS.includes(archivo.type);
  const porExtension = EXTENSIONES_PERMITIDAS.some((ext) =>
    archivo.name.toLowerCase().endsWith(ext),
  );
  // Basta con que uno de los dos criterios pase — el MIME no siempre
  // es confiable para .csv según navegador/SO.
  return porMime || porExtension;
}

export const UploadFormSchema = z.object({
  archivo: z
    .instanceof(File, { message: "Debes seleccionar un archivo" })
    .refine(tieneFormatoValido, {
      message: "Formato no soportado. Usa .xlsx, .xls o .csv",
    })
    .refine((f) => f.size <= TAMANO_MAXIMO_BYTES, {
      message: "El archivo supera el tamaño máximo permitido (10MB)",
    }),
});

export type UploadFormValues = z.infer<typeof UploadFormSchema>;
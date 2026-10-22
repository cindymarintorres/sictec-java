import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRef, useState } from "react";
import {
  UploadFormSchema,
  type UploadFormValues,
} from "@/schemas/upload.schema";
import { useSubirLote } from "../hooks/useLotes";

export const UploadPage = () => {
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const subirLoteMutation = useSubirLote();

  const {
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<UploadFormValues>({
    resolver: zodResolver(UploadFormSchema),
  });

  const archivo = watch("archivo");
  const isDisabled = !archivo || subirLoteMutation.isPending;

  const seleccionarArchivo = (file: File) =>
    setValue("archivo", file, { shouldValidate: true });

  const onDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) seleccionarArchivo(file);
  };

  const onSubmit = (values: UploadFormValues) => {
    subirLoteMutation.mutate(values.archivo);
  };

  return (
    <div className="flex flex-col items-center gap-6 max-w-xl mx-auto">
      <h2 className="text-2xl font-bold text-primary">
        Procesamiento de datos
      </h2>

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="w-full flex flex-col gap-4"
      >
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={onDrop}
          onClick={() => inputRef.current?.click()}
          className={`border-2 border-dashed rounded-lg p-10 py-20 text-center cursor-pointer transition-colors ${
            isDragging
              ? "border-primary bg-primary/5"
              : "border-muted-foreground/30"
          }`}
        >
          <input
            ref={inputRef}
            type="file"
            accept=".xlsx,.xls,.csv"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) seleccionarArchivo(file);
            }}
          />
          <p className="font-medium">
            {archivo
              ? archivo.name
              : "Arrastra tu archivo aquí, o haz clic para seleccionar."}
          </p>
          <p className="text-sm text-muted-foreground mt-1">
            Formatos aceptados: .xlsx, .xls, .csv — Columna requerida:
            RUC_EMISOR
          </p>
        </div>

        {errors.archivo && (
          <p className="text-sm text-destructive">{errors.archivo.message}</p>
        )}

        <button
          type="submit"
          disabled={isDisabled}
          className={`bg-primary text-primary-foreground rounded-md py-2 font-semibold disabled:opacity-50 ${
            isDisabled ? "cursor-not-allowed" : "cursor-pointer"
          }`}
        >
          {subirLoteMutation.isPending ? "Subiendo..." : "Procesar archivo"}
        </button>

        {subirLoteMutation.isError && (
          <p className="text-sm text-destructive">
            No se pudo subir el archivo. Intenta de nuevo.
          </p>
        )}
      </form>
    </div>
  );
};

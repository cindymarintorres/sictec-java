import { useEffect, useRef, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router";
import { CheckCircle2, XCircle } from "lucide-react";
import {
  Empty,
  EmptyContent,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from "@/components/ui/empty";
import { Spinner } from "@/components/ui/spinner";
import { Button, buttonVariants } from "@/components/ui/button";
import { useCancelarLote, useEstadoLote } from "../hooks/useLotes";
import { cn } from "@/lib/utils";

export const LoteStatusPage = () => {
  const { batchId } = useParams<{ batchId: string }>();
  const navigate = useNavigate();
  const { data: estado, isLoading, isError } = useEstadoLote(batchId!);
  const location = useLocation();
  const [pareceAtascado, setPareceAtascado] = useState(false);
  const infoSubida = location.state as {
    totalFilas?: number;
    duplicados?: number;
  } | null;

  const cancelarMutation = useCancelarLote(batchId!);

  const ultimoProgreso = useRef<{ processed: number; timestamp: number }>({
    processed: -1,
    timestamp: 0, // valor estático, no una llamada impura
  });
  useEffect(() => {
    if (!estado || estado.finished) return;

    if (estado.processed_jobs !== ultimoProgreso.current.processed) {
      ultimoProgreso.current = {
        processed: estado.processed_jobs,
        timestamp: Date.now(),
      };
      setPareceAtascado(false);
      return;
    }

    const segundosSinAvanzar =
      (Date.now() - ultimoProgreso.current.timestamp) / 1000;
    if (segundosSinAvanzar > 30) {
      setPareceAtascado(true);
    }
  }, [estado]);

  if (isError) {
    return (
      <Empty className="w-full">
        <EmptyHeader className="max-w-md!">
          <XCircle className="text-destructive size-15" />
          <EmptyTitle className="text-2xl font-medium">
            Lote no encontrado
          </EmptyTitle>
          <EmptyDescription className="text-md">
            No se encontró el lote solicitado. Puede que el enlace sea
            incorrecto.
          </EmptyDescription>
        </EmptyHeader>
        <EmptyContent>
          <Button
            className="bg-blue-900 cursor-pointer"
            size="lg"
            onClick={() => navigate("/")}
          >
            Volver al inicio
          </Button>
        </EmptyContent>
      </Empty>
    );
  }

  if (isLoading || !estado) {
    return (
      <Empty className="w-full">
        <EmptyHeader className="max-w-md!">
          <Spinner className="size-15" />
          <EmptyTitle className="text-md">Cargando estado del lote</EmptyTitle>
        </EmptyHeader>
      </Empty>
    );
  }

  if (!estado.finished) {
    return (
      <Empty className="w-full">
        <EmptyHeader className="max-w-md!">
          <Spinner className="size-15" />
          <EmptyTitle className="text-2xl font-medium">
            Procesando Clasificaciones Tributarias
          </EmptyTitle>

          <EmptyDescription className="text-md">
            {estado.processed_jobs} de {estado.total_jobs} RUC procesados.
            Consultando el SRI y clasificando actividades económicas. No cierres
            esta pestaña.
          </EmptyDescription>

          {infoSubida && (infoSubida.duplicados ?? 0) > 0 && (
            <EmptyDescription className="text-md">
              {infoSubida.totalFilas} filas recibidas — {infoSubida.duplicados}{" "}
              RUC duplicados fueron consolidados antes de consultar el SRI.
            </EmptyDescription>
          )}
          {pareceAtascado && (
            <EmptyDescription className="text-yellow-600">
              El procesamiento no ha avanzado en los últimos segundos. Puede
              tomar más tiempo de lo normal, o haber un problema temporal — si
              persiste, contacta soporte.
            </EmptyDescription>
          )}
        </EmptyHeader>
        <EmptyContent>
          <Button
            className="bg-blue-900 cursor-pointer"
            size="lg"
            onClick={() => cancelarMutation.mutate()}
            disabled={cancelarMutation.isPending}
          >
            {cancelarMutation.isPending
              ? "Cancelando..."
              : "Cancelar operación"}
          </Button>
        </EmptyContent>
      </Empty>
    );
  }

  if (estado.cancelled) {
    return (
      <Empty className="w-full">
        <EmptyHeader className="max-w-md!">
          <XCircle className="text-yellow-600 size-15" />
          <EmptyTitle className="text-2xl font-medium">
            Procesamiento cancelado
          </EmptyTitle>
          <EmptyDescription className="text-md">
            Se procesaron {estado.processed_jobs} de {estado.total_jobs} RUC
            antes de cancelar. Los RUC restantes aparecerán como "No encontrado"
            en el archivo.
          </EmptyDescription>
        </EmptyHeader>
        <EmptyContent className="flex flex-1 flex-row justify-center gap-2 items-center mt-5">
          <a
            href={`/api/lotes/${batchId}/descargar`}
            download
            className={cn(
              buttonVariants({ variant: "default", size: "lg" }),
              "bg-yellow-700 hover:bg-yellow-600",
            )}
          >
            Descargar resultado parcial
          </a>
          <Button
            className="bg-blue-900 cursor-pointer"
            size="lg"
            onClick={() => navigate("/")}
          >
            Procesar otro archivo
          </Button>
        </EmptyContent>
      </Empty>
    );
  }

  return (
    <Empty className="w-full">
      <EmptyHeader className="max-w-md!">
        <CheckCircle2 className="text-green-600 size-15" />
        <EmptyTitle className="text-2xl font-medium">
          ¡Procesamiento completo!
        </EmptyTitle>
        <EmptyDescription className="text-md">
          El archivo se procesó correctamente y las columnas{" "}
          <strong>ACTIVIDAD_ECONOMICA y CATEGORIA</strong> fueron agregadas.
        </EmptyDescription>
      </EmptyHeader>
      <EmptyContent className="flex flex-1 flex-row justify-center gap-2 items-center mt-5">
        <a
          href={`/api/lotes/${batchId}/descargar`}
          download
          className={cn(
            buttonVariants({ variant: "default", size: "lg" }),
            "bg-green-800 hover:bg-green-700",
          )}
        >
          Descargar resultado
        </a>

        <Button
          className="bg-blue-900 cursor-pointer"
          size="lg"
          onClick={() => navigate("/")}
        >
          Procesar otro archivo
        </Button>
      </EmptyContent>
    </Empty>
  );
};

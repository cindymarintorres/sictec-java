import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router";
import { lotesService } from "@/services/lotesService";

export const useSubirLote = () => {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (archivo: File) => lotesService.subirLote(archivo),
    onSuccess: (data) => {
      navigate(`/lotes/${data.batch_id}`, {
        state: {
          totalFilas: data.total_filas,
          duplicados: data.duplicados,
        },
      });
    },
  });
};

export const useEstadoLote = (batchId: string) => {
  return useQuery({
    queryKey: ["lote", batchId],
    queryFn: () => lotesService.obtenerEstadoLote(batchId),
    refetchInterval: (query) => {
      // Deja de hacer polling en cuanto el batch termina
      return query.state.data?.finished ? false : 2000;
    },
    retry: 2, // no reintentar infinito ante fallos de red/servidor
  });
};

export const useCancelarLote = (batchId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => lotesService.cancelarLote(batchId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["lote", batchId] });
    },
  });
};

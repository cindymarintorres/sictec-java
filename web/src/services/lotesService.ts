import { api } from "@/lib/api";
import {
  EstadoLoteSchema,
  SubirLoteResponseSchema,
  type EstadoLote,
  type SubirLoteResponse,
} from "../schemas/lotes.schema";

export const lotesService = {
  subirLote: async (archivo: File): Promise<SubirLoteResponse> => {
    const formData = new FormData();
    formData.append("archivo", archivo);

    const { data } = await api.post("/lotes", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    return SubirLoteResponseSchema.parse(data);
  },

  obtenerEstadoLote: async (batchId: string): Promise<EstadoLote> => {
    const { data } = await api.get(`/lotes/${batchId}`);
    return EstadoLoteSchema.parse(data);
  },

  cancelarLote: async (batchId: string): Promise<void> => {
    await api.post(`/lotes/${batchId}/cancelar`);
  },
};

import { z } from "zod";

export const SubirLoteResponseSchema = z.object({
  batch_id: z.string(),
  total_filas: z.number(),
  rucs_unicos: z.number(),
  duplicados: z.number(),
});

export const EstadoLoteSchema = z.object({
  total_jobs: z.number(),
  pending_jobs: z.number(),
  processed_jobs: z.number(),
  failed_jobs: z.number(),
  finished: z.boolean(),
  cancelled: z.boolean(),
});

export type SubirLoteResponse = z.infer<typeof SubirLoteResponseSchema>;
export type EstadoLote = z.infer<typeof EstadoLoteSchema>;
import { z } from "zod";

const envSchema = z.object({
  VITE_SHOW_REACT_QUERY_DEVTOOLS: z
    .string()
    .optional()
    .transform((val) => val === "true"),
});

const parsed = envSchema.safeParse({
  VITE_SHOW_REACT_QUERY_DEVTOOLS: import.meta.env.VITE_SHOW_REACT_QUERY_DEVTOOLS,
});

if (!parsed.success) {
  const messages = parsed.error.issues
    .map((issue) => `  ${issue.path.join(".")}: ${issue.message}`)
    .join("\n");

  throw new Error(`\n Variables de entorno inválidas:\n${messages}\n`);
}

export const env = parsed.data;
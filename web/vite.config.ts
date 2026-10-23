import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    server: {
      host: "0.0.0.0",
      port: Number(env.WEB_PORT) || 3001,
      proxy: {
        "/api": {
          // Usamos la variable cargada o el fallback
          target: "http://localhost:8080",
          changeOrigin: true,
        },
      }
    },
  };
});

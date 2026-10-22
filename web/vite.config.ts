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
          target: env.VITE_PROXY_TARGET || "http://sictec_api:8000",
          changeOrigin: true,
          // REVISA ESTO: Laravel suele tener el prefijo /api en sus rutas.
          // Si tu backend ya espera /api/algo, NO uses el rewrite.
          // rewrite: (path) => path.replace(/^\/api/, ''),
          configure: (proxy) => { 
            //Manejar el API_KEY desde el proxy de Vite para no exponer la misma
            //ya que corre en Node, del lado del servidor de desarrollo, nunca se descarga al cliente). Así la key nunca toca el bundle
            proxy.on("proxyReq", (proxyReq) => {
              proxyReq.setHeader("X-API-KEY", env.SICTEC_API_KEY ?? "");
            });
          },
        },
      },
      watch: {
        usePolling: true,
      },
    },
  };
});

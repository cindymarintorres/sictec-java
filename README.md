# 📋 SICTEC (Java Edition) — Sistema Inteligente de Clasificación Tributaria y Contable

**Herramienta de escritorio / local para clasificar facturas del SRI de Ecuador por categoría de gasto a partir de archivos Excel o CSV.**

> El usuario sube un archivo con facturas (columna `RUC_EMISOR`). El sistema deduplica los RUC, consulta la actividad económica de cada uno en los servicios web públicos del SRI, la mapea a una categoría de gasto (Combustible, Repuestos, Farmacia, Seguros, etc.) mediante un diccionario oficial de reglas jerárquicas, y genera el archivo original con dos nuevas columnas: `ACTIVIDAD_ECONOMICA` y `CATEGORIA`, listo para descargar.

---

## 🔄 Sobre esta versión (Migración desde PHP/Laravel)

Este repositorio es la **reescritura moderna en Java 21 / Spring Boot** del proyecto original en Laravel. 

* **¿Por qué la migración?** El proyecto original requería Docker Desktop, contenedores PostgreSQL, workers separados y Git en la máquina del usuario final, lo que resultaba complejo y pesado para entornos contables u oficinas estándar de Windows.
* **El objetivo de esta versión:** Generar un único ejecutable (`.jar` o instalador/ejecutable `.exe` vía `jpackage`) donde el servidor backend, la base de datos embebida (H2) y el frontend en React convivan en un solo proceso.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Propósito |
|:---|:---|:---|
| **Lenguaje Backend** | ☕ Java 21 (LTS) | Uso de Virtual Threads (Loom) para alta concurrencia en llamadas HTTP al SRI |
| **Framework Backend**| 🍃 Spring Boot 4.x | Inversión de Control (IoC), RestClient, Spring Data JPA y servidor embebido |
| **Build Tool** | 🐘 Gradle (Groovy DSL) | Gestión de dependencias, compilación y empaquetado `bootJar` |
| **Base de Datos** | 🗄️ H2 Database (modo archivo) | Almacenamiento local persistente (`./data/sictec.mv.db`) para estado de lotes |
| **Procesamiento Asíncrono** | ⚙️ Spring `@Async` + `CompletableFuture` | Consultas en segundo plano sin dependencias de colas externas |
| **Manipulación Excel** | 📊 Apache POI (`poi`, `poi-ooxml`) | Lectura y escritura de `.xlsx` / `.xls` preservando formato de texto |
| **Cliente HTTP** | 🌐 Spring `RestClient` | Conexión con reintentos y timeouts a la API pública del SRI |
| **Frontend** | ⚛️ React 19 + TypeScript + Vite | SPA moderna con Tailwind CSS v4, shadcn/ui y TanStack React Query |
| **Empaquetado** | 📦 Fat JAR + `jpackage` | Aplicación ejecutable de doble clic sin requerir Java preinstalado |

---

## 🏗️ Estructura del Proyecto

```
sictec-java/
├── api/                                          # 🍃 Backend Spring Boot
│   ├── build.gradle                              # Dependencias y tareas de compilación
│   ├── settings.gradle
│   ├── src/main/java/com/sictec/spring/api/
│   │   ├── SictecApplication.java                # Clase principal (@SpringBootApplication)
│   │   ├── controller/                           # Controladores REST (@RestController)
│   │   ├── service/                              # Lógica de negocio (@Service)
│   │   ├── excel/                                # Lector y escritor Apache POI
│   │   ├── model/                                # Entidades JPA (@Entity)
│   │   ├── repository/                           # Interfaces JPA (@Repository)
│   │   └── config/                               # Clases de configuración (@Configuration)
│   └── src/main/resources/
│       ├── application.properties                # Configuración de H2, puerto y logging
│       ├── data/                                 # Catálogos de actividades y categorías (JSON/YAML)
│       └── static/                               # Build compilado de React (dist) en producción
│
├── web/                                          # ⚛️ Frontend React 19 + Vite
│   ├── src/
│   │   ├── components/                           # Componentes visuales UI (shadcn/ui)
│   │   ├── features/upload/                      # Formularios de subida y monitoreo de progreso
│   │   ├── hooks/                                # React Query Hooks
│   │   ├── services/                             # Clientes HTTP (Axios)
│   │   └── lib/                                  # Utilidades
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

---

## 🚀 Entorno de Desarrollo Local

En desarrollo se ejecutan el backend y el frontend de forma independiente para aprovechar el recargo en caliente (*hot reload*):

```bash
# 1. Terminal 1: Iniciar el backend Spring Boot
cd api
./gradlew bootRun

# 2. Terminal 2: Iniciar el frontend Vite
cd web
pnpm install
pnpm dev
```

### 🌐 URLs en Desarrollo

| Servicio | URL | Descripción |
|:---|:---|:---|
| 🖥️ **Frontend (Vite Dev)** | `http://localhost:3001` | Interfaz gráfica con HMR |
| 🍃 **API Backend** | `http://localhost:8080` | Servidor REST Spring Boot |
| 🗄️ **Consola H2** | `http://localhost:8080/h2-console` | Explorador web de la base de datos local |

---

## 📦 Generación del Ejecutable Único (Producción)

Para generar la versión final de **doble clic** donde no se requiere Node.js:

```bash
# 1. Compilar los recursos estáticos de React
cd web
pnpm build

# 2. Copiar los archivos a los recursos estáticos de Spring Boot
cp -r dist/* ../api/src/main/resources/static/

# 3. Empaquetar el JAR autocontenido (incluye backend + frontend + Tomcat embebido)
cd ../api
./gradlew bootJar

# 4. Ejecutar el JAR resultante
java -jar build/libs/api-0.0.1-SNAPSHOT.jar
```

El usuario final simplemente ejecuta el archivo y abre `http://localhost:8080`.

### 🪟 Generación del instalador Windows (`sictec-java.exe`) con GitHub Actions

Para generar el instalador nativo de Windows sin necesidad de compilar en una máquina Windows física:

1. El repositorio incluye el workflow [`.github/workflows/build-windows.yml`](.github/workflows/build-windows.yml).
2. En GitHub, ve a la pestaña **Actions** $\rightarrow$ selecciona **Build Windows Executable (.exe)** $\rightarrow$ **Run workflow**.
3. El pipeline compila React, Spring Boot y ejecuta `jpackage` en una máquina virtual Windows oficial.
4. Al finalizar, descarga el artefacto **`sictec-java-windows-installer`**, que contiene el ejecutable `sictec-java.exe` con Java embebido (cero dependencias para el cliente final).

---

## 📡 Endpoints de la API

```
❤️ Health
  GET    /api/health                 # Estado del backend

📤 Lotes de Facturas
  POST   /api/lotes                  # Sube el Excel/CSV, inicia el procesamiento asíncrono
  GET    /api/lotes/{batchId}        # Consulta el progreso (procesados, pendientes, fallidos)
  GET    /api/lotes/{batchId}/descargar # Descarga el Excel procesado con las clasificaciones
  POST   /api/lotes/{batchId}/cancelar  # Cancela el procesamiento del lote
```
---

## 📋 Estado del proyecto

Este proyecto está en desarrollo activo como parte de un portafolio de ingeniería de software. La arquitectura, decisiones de diseño y progreso de implementación están documentados en el historial de commits.
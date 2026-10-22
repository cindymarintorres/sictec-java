import AppRouter from "./app/router/AppRouter";
import { Toaster } from "@/components/ui/sonner";

const App = () => (
  <>
    <AppRouter />
    {/* Instancia Unica */}
    <Toaster
      position="bottom-center"
      toastOptions={{
        //duration: Infinity, //DEBUG: quitar cuando ya funcione
        classNames: {
          toast: "!w-auto",
          success: "!bg-green-100 dark:!bg-green-900 !text-green-800 dark:!text-green-100 !border-0",
          error: "!bg-red-100 dark:!bg-red-900 !text-red-800 dark:!text-red-100 !border-0",
          warning: "!bg-yellow-100 dark:!bg-yellow-900 !text-yellow-800 dark:!text-yellow-100 !border-0",
          info: "!bg-blue-100 dark:!bg-blue-900 !text-blue-800 dark:!text-blue-100 !border-0",
        },
      }}
    />
  </>
);

export default App;
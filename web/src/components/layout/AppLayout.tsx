import { Outlet } from "react-router";
import { ThemeToggle } from "@/components/shared/ThemeToggle";

export const AppLayout = () => {
  const currentYear = new Date().getFullYear(); // Obtiene el año actual

  return (
    <>
      <div className="min-h-screen flex flex-col">
        <header className="flex h-15 shrink-0 items-center justify-between gap-2 border-b rounded-b-lg px-4 shadow-xl">
          <h1 className="text-3xl font-bold text-primary text-shadow-lg/20">
            SICTEC
          </h1>
          <ThemeToggle />
        </header>

        <div className="flex flex-1 overflow-y-auto p-6 items-center justify-center">
          <Outlet />
        </div>

        <footer className="flex h-15 shrink-0 items-center justify-center gap-2 border-t rounded-t-lg px-4 inset-shadow-sm">
          <h5 className="font-semibold text-primary">
            &copy; {currentYear} SICTEC. Todos los derechos reservados.
          </h5>
        </footer>
      </div>
    </>
  );
};

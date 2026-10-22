import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { BrowserRouter } from "react-router";
import { ThemeProvider } from "next-themes";
import type { ReactNode } from "react";
import { env } from "@/config/env";

const queryClient = new QueryClient();

type Props = { children: ReactNode };

export const AppProviders = ({ children }: Props) => (
  <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
          {children}
      </BrowserRouter>
      {env.VITE_SHOW_REACT_QUERY_DEVTOOLS && (
        <ReactQueryDevtools initialIsOpen={false} />
      )}
    </QueryClientProvider>
  </ThemeProvider>
);
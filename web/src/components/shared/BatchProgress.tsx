import { LoaderIcon } from "lucide-react";

type BatchProgressProps = {
  processed: number;
  total: number;
};

export function BatchProgress({ processed, total }: BatchProgressProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-12">
      <LoaderIcon className="size-6 animate-spin text-muted-foreground" />
      <p className="text-sm text-muted-foreground">
        {processed} de {total} RUC procesados
      </p>
    </div>
  );
}
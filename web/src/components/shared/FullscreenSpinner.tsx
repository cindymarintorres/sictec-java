import { LoaderIcon } from "lucide-react";
import { cn } from "@/lib/utils";

function Spinner({ className, ...props }: React.ComponentProps<"svg">) {
  return (
    <LoaderIcon
      role="status"
      aria-label="Loading"
      className={cn("size-16 animate-spin text-primary", className)}
      {...props}
    />
  );
}

export function FullscreenSpinner() {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/10 backdrop-blur-sm animate-in fade-in duration-200">
      <Spinner />
    </div>
  );
}
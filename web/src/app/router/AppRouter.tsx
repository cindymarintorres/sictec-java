import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router";
import { AppLayout } from "@/components/layout/AppLayout";
import { FullscreenSpinner } from "@/components/shared/FullscreenSpinner";

const UploadPage = lazy(() =>
  import("@/features/upload/pages/UploadPage").then((m) => ({
    default: m.UploadPage,
  })),
);

const LoteStatusPage = lazy(() =>
  import("@/features/upload/pages/LoteStatusPage").then((m) => ({ default: m.LoteStatusPage })),
);

const AppRouter = () => (
  <Suspense fallback={<FullscreenSpinner />}>
    <Routes>
      <Route element={<AppLayout />}>
        {/* Pública */}
        <Route path="/" element={<UploadPage />} />
        <Route path="/lotes/:batchId" element={<LoteStatusPage />} />

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  </Suspense>
);

export default AppRouter;
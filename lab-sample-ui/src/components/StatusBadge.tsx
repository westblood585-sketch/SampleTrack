import type { SampleStatus } from "../types";

const STYLES: Record<SampleStatus, string> = {
  RECEIVED: "bg-slate-100 text-slate-700",
  IN_PROGRESS: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-emerald-100 text-emerald-700",
  REJECTED: "bg-rose-100 text-rose-700",
};

const LABELS: Record<SampleStatus, string> = {
  RECEIVED: "Kabul Edildi",
  IN_PROGRESS: "İşlemde",
  COMPLETED: "Tamamlandı",
  REJECTED: "Reddedildi",
};

const DOTS: Record<SampleStatus, string> = {
  RECEIVED: "bg-slate-500",
  IN_PROGRESS: "bg-blue-500",
  COMPLETED: "bg-emerald-500",
  REJECTED: "bg-rose-500",
};

export function StatusBadge({ status }: { status: SampleStatus }) {
  return (
    <span className={`badge ${STYLES[status]}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${DOTS[status]}`} />
      {LABELS[status]}
    </span>
  );
}

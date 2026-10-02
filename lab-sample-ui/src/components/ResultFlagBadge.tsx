import type { ResultFlag } from "../types";

const STYLES: Record<ResultFlag, string> = {
  NORMAL: "bg-emerald-100 text-emerald-700",
  LOW: "bg-amber-100 text-amber-700",
  HIGH: "bg-rose-100 text-rose-700",
  NOT_APPLICABLE: "bg-slate-100 text-slate-500",
};

const LABELS: Record<ResultFlag, string> = {
  NORMAL: "Normal",
  LOW: "Düşük",
  HIGH: "Yüksek",
  NOT_APPLICABLE: "—",
};

export function ResultFlagBadge({ flag }: { flag: ResultFlag }) {
  if (flag === "NOT_APPLICABLE") return <span className="text-xs text-slate-400">—</span>;
  return <span className={`badge ${STYLES[flag]}`}>{LABELS[flag]}</span>;
}

import type { SampleStatus } from "../types";

const STEPS: { status: SampleStatus; label: string }[] = [
  { status: "RECEIVED", label: "Kabul Edildi" },
  { status: "IN_PROGRESS", label: "İşlemde" },
  { status: "COMPLETED", label: "Tamamlandı" },
];

export function SampleStageStepper({ status }: { status: SampleStatus }) {
  if (status === "REJECTED") {
    return (
      <div className="flex items-center gap-2 rounded-lg bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-rose-600 text-xs text-white">
          ✕
        </span>
        Numune reddedildi — akış burada durdu
      </div>
    );
  }

  const currentIndex = STEPS.findIndex((s) => s.status === status);

  return (
    <div className="flex items-center">
      {STEPS.map((step, idx) => {
        const done = idx < currentIndex;
        const active = idx === currentIndex;
        return (
          <div key={step.status} className="flex flex-1 items-center last:flex-none">
            <div className="flex flex-col items-center gap-1.5">
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold ${
                  done
                    ? "bg-brand-600 text-white"
                    : active
                      ? "bg-brand-100 text-brand-700 ring-2 ring-brand-500"
                      : "bg-slate-100 text-slate-400"
                }`}
              >
                {done ? "✓" : idx + 1}
              </div>
              <span
                className={`text-xs font-medium ${active ? "text-brand-700" : done ? "text-slate-700" : "text-slate-400"}`}
              >
                {step.label}
              </span>
            </div>
            {idx < STEPS.length - 1 && (
              <div className={`mx-2 h-0.5 flex-1 ${done ? "bg-brand-600" : "bg-slate-200"}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

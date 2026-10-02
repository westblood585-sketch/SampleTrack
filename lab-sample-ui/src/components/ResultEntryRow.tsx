import { useState } from "react";
import { ResultFlagBadge } from "./ResultFlagBadge";
import { formatDateTime, formatNumber } from "../lib/format";
import type { SampleTest } from "../types";

interface Props {
  test: SampleTest;
  disabled: boolean;
  onSubmit: (value: number, enteredBy: string) => Promise<void>;
}

export function ResultEntryRow({ test, disabled, onSubmit }: Props) {
  const [value, setValue] = useState("");
  const [enteredBy, setEnteredBy] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const range =
    test.refMin != null && test.refMax != null
      ? `${formatNumber(test.refMin)}–${formatNumber(test.refMax)} ${test.unit ?? ""}`
      : "—";

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const numeric = Number(value);
    if (Number.isNaN(numeric) || !enteredBy.trim()) return;
    setSubmitting(true);
    try {
      await onSubmit(numeric, enteredBy.trim());
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <tr className="hover:bg-slate-50">
      <td className="px-5 py-3">
        <p className="font-medium text-slate-900">{test.code}</p>
        <p className="text-xs text-slate-500">{test.name}</p>
      </td>
      <td className="px-5 py-3 text-sm text-slate-500">{range}</td>
      <td className="px-5 py-3">
        {test.result ? (
          <div className="flex items-center gap-2">
            <span className="font-medium text-slate-900">
              {formatNumber(test.result.value)} {test.unit}
            </span>
            <ResultFlagBadge flag={test.result.flag} />
          </div>
        ) : disabled ? (
          <span className="text-sm text-slate-400">Sonuç girilemez</span>
        ) : (
          <form onSubmit={handleSubmit} className="flex items-center gap-2">
            <input
              className="input w-28"
              placeholder="Değer"
              inputMode="decimal"
              value={value}
              onChange={(e) => setValue(e.target.value)}
              required
            />
            <input
              className="input w-36"
              placeholder="Giren kişi"
              value={enteredBy}
              onChange={(e) => setEnteredBy(e.target.value)}
              required
            />
            <button type="submit" className="btn-secondary" disabled={submitting}>
              {submitting ? "…" : "Kaydet"}
            </button>
          </form>
        )}
      </td>
      <td className="px-5 py-3 text-xs text-slate-400">
        {test.result ? `${test.result.enteredBy} · ${formatDateTime(test.result.enteredAt)}` : "—"}
      </td>
    </tr>
  );
}

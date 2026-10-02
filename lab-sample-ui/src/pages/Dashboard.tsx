import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { samplesApi } from "../api/samples";
import { StatusBadge } from "../components/StatusBadge";
import { Spinner } from "../components/Spinner";
import { ErrorBanner } from "../components/ErrorBanner";
import { formatDateTime, errorMessage } from "../lib/format";
import type { SampleStatus, SampleSummary } from "../types";

const STATUSES: SampleStatus[] = ["RECEIVED", "IN_PROGRESS", "COMPLETED", "REJECTED"];

const STATUS_CARD_LABEL: Record<SampleStatus, string> = {
  RECEIVED: "Kabul Edildi",
  IN_PROGRESS: "İşlemde",
  COMPLETED: "Tamamlandı",
  REJECTED: "Reddedildi",
};

export default function Dashboard() {
  const [counts, setCounts] = useState<Record<SampleStatus, number> | null>(null);
  const [recent, setRecent] = useState<SampleSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const [recentPage, ...countPages] = await Promise.all([
          samplesApi.search({ page: 0, size: 6 }),
          ...STATUSES.map((status) => samplesApi.search({ status, page: 0, size: 1 })),
        ]);
        if (cancelled) return;
        setRecent(recentPage.content);
        const next = {} as Record<SampleStatus, number>;
        STATUSES.forEach((status, idx) => {
          next[status] = countPages[idx].totalElements;
        });
        setCounts(next);
      } catch (err) {
        if (!cancelled) setError(errorMessage(err));
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Panel</h1>
        <p className="mt-1 text-sm text-slate-500">Laboratuvar numune akışına genel bakış.</p>
      </div>

      {error && <ErrorBanner message={error} />}
      {loading && <Spinner />}

      {counts && (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {STATUSES.map((status) => (
            <Link
              key={status}
              to={`/samples?status=${status}`}
              className="card flex flex-col gap-2 p-5 transition-shadow hover:shadow-md"
            >
              <StatusBadge status={status} />
              <span className="text-3xl font-bold text-slate-900">{counts[status]}</span>
              <span className="text-xs text-slate-500">{STATUS_CARD_LABEL[status]} numune</span>
            </Link>
          ))}
        </div>
      )}

      <div className="card">
        <div className="flex items-center justify-between border-b border-slate-100 px-5 py-4">
          <h2 className="font-semibold text-slate-900">Son Numuneler</h2>
          <Link to="/samples" className="text-sm font-medium text-brand-600 hover:text-brand-700">
            Tümünü gör →
          </Link>
        </div>
        <ul className="divide-y divide-slate-100">
          {recent.map((sample) => (
            <li key={sample.id}>
              <Link
                to={`/samples/${sample.id}`}
                className="flex items-center justify-between px-5 py-3 hover:bg-slate-50"
              >
                <div>
                  <p className="font-medium text-slate-900">{sample.barcode}</p>
                  <p className="text-xs text-slate-500">
                    {sample.customerName} · {formatDateTime(sample.receivedAt)}
                  </p>
                </div>
                <StatusBadge status={sample.status} />
              </Link>
            </li>
          ))}
          {!loading && recent.length === 0 && (
            <li className="px-5 py-8 text-center text-sm text-slate-400">Henüz numune kaydı yok.</li>
          )}
        </ul>
      </div>
    </div>
  );
}

import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { samplesApi } from "../api/samples";
import { StatusBadge } from "../components/StatusBadge";
import { Spinner } from "../components/Spinner";
import { ErrorBanner } from "../components/ErrorBanner";
import { formatDateTime, errorMessage } from "../lib/format";
import type { Page, SampleStatus, SampleSummary } from "../types";

const STATUS_OPTIONS: { value: SampleStatus | ""; label: string }[] = [
  { value: "", label: "Tüm durumlar" },
  { value: "RECEIVED", label: "Kabul Edildi" },
  { value: "IN_PROGRESS", label: "İşlemde" },
  { value: "COMPLETED", label: "Tamamlandı" },
  { value: "REJECTED", label: "Reddedildi" },
];

export default function SampleList() {
  const [params, setParams] = useSearchParams();
  const status = (params.get("status") as SampleStatus | null) ?? "";
  const barcodeParam = params.get("barcode") ?? "";
  const [barcodeInput, setBarcodeInput] = useState(barcodeParam);
  const [page, setPage] = useState<Page<SampleSummary> | null>(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    samplesApi
      .search({ status: status || undefined, barcode: barcodeParam || undefined, page: pageNumber })
      .then((result) => !cancelled && setPage(result))
      .catch((err) => !cancelled && setError(errorMessage(err)))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [status, barcodeParam, pageNumber]);

  function updateStatus(next: string) {
    setPageNumber(0);
    const nextParams = new URLSearchParams(params);
    if (next) nextParams.set("status", next);
    else nextParams.delete("status");
    setParams(nextParams);
  }

  function submitBarcodeSearch(e: React.FormEvent) {
    e.preventDefault();
    setPageNumber(0);
    const nextParams = new URLSearchParams(params);
    if (barcodeInput) nextParams.set("barcode", barcodeInput);
    else nextParams.delete("barcode");
    setParams(nextParams);
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Numuneler</h1>
          <p className="mt-1 text-sm text-slate-500">Tüm numuneleri filtreleyerek görüntüleyin.</p>
        </div>
        <Link to="/samples/new" className="btn-primary">
          + Numune Kabul Et
        </Link>
      </div>

      <div className="flex flex-wrap items-end gap-3">
        <div>
          <label className="label">Durum</label>
          <select
            className="input w-48"
            value={status}
            onChange={(e) => updateStatus(e.target.value)}
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        <form onSubmit={submitBarcodeSearch} className="flex items-end gap-2">
          <div>
            <label className="label">Barkod ara</label>
            <input
              className="input w-56"
              placeholder="BC-2026-..."
              value={barcodeInput}
              onChange={(e) => setBarcodeInput(e.target.value)}
            />
          </div>
          <button type="submit" className="btn-secondary">
            Ara
          </button>
        </form>
      </div>

      {error && <ErrorBanner message={error} />}
      {loading && <Spinner />}

      {page && !loading && (
        <div className="card overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3 font-medium">Barkod</th>
                <th className="px-5 py-3 font-medium">Müşteri</th>
                <th className="px-5 py-3 font-medium">Durum</th>
                <th className="px-5 py-3 font-medium">Kabul Zamanı</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {page.content.map((sample) => (
                <tr key={sample.id} className="hover:bg-slate-50">
                  <td className="px-5 py-3">
                    <Link to={`/samples/${sample.id}`} className="font-medium text-brand-600 hover:underline">
                      {sample.barcode}
                    </Link>
                  </td>
                  <td className="px-5 py-3 text-slate-600">{sample.customerName}</td>
                  <td className="px-5 py-3">
                    <StatusBadge status={sample.status} />
                  </td>
                  <td className="px-5 py-3 text-slate-500">{formatDateTime(sample.receivedAt)}</td>
                </tr>
              ))}
              {page.content.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-5 py-10 text-center text-slate-400">
                    Filtreye uyan numune bulunamadı.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          {page.totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-slate-100 px-5 py-3 text-sm text-slate-500">
              <span>
                Sayfa {page.number + 1} / {page.totalPages} · {page.totalElements} numune
              </span>
              <div className="flex gap-2">
                <button
                  className="btn-secondary"
                  disabled={page.number === 0}
                  onClick={() => setPageNumber((n) => Math.max(0, n - 1))}
                >
                  Önceki
                </button>
                <button
                  className="btn-secondary"
                  disabled={page.number + 1 >= page.totalPages}
                  onClick={() => setPageNumber((n) => n + 1)}
                >
                  Sonraki
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

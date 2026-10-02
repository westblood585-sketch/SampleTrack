import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { samplesApi } from "../api/samples";
import { SampleStageStepper } from "../components/SampleStageStepper";
import { ResultEntryRow } from "../components/ResultEntryRow";
import { ErrorBanner } from "../components/ErrorBanner";
import { Spinner } from "../components/Spinner";
import { formatDateTime, errorMessage } from "../lib/format";
import type { SampleDetail as SampleDetailType } from "../types";

export default function SampleDetail() {
  const { id } = useParams<{ id: string }>();
  const sampleId = Number(id);
  const [sample, setSample] = useState<SampleDetailType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [actionPending, setActionPending] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    return samplesApi
      .get(sampleId)
      .then(setSample)
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [sampleId]);

  useEffect(() => {
    load();
  }, [load]);

  async function handleStart() {
    setActionPending(true);
    setActionError(null);
    try {
      setSample(await samplesApi.start(sampleId));
    } catch (err) {
      setActionError(errorMessage(err));
    } finally {
      setActionPending(false);
    }
  }

  async function handleReject(e: React.FormEvent) {
    e.preventDefault();
    if (!rejectReason.trim()) return;
    setActionPending(true);
    setActionError(null);
    try {
      setSample(await samplesApi.reject(sampleId, rejectReason.trim()));
      setShowRejectForm(false);
      setRejectReason("");
    } catch (err) {
      setActionError(errorMessage(err));
    } finally {
      setActionPending(false);
    }
  }

  async function handleComplete() {
    setActionPending(true);
    setActionError(null);
    try {
      setSample(await samplesApi.complete(sampleId));
    } catch (err) {
      setActionError(errorMessage(err));
    } finally {
      setActionPending(false);
    }
  }

  async function handleResultSubmit(sampleTestId: number, value: number, enteredBy: string) {
    setActionError(null);
    try {
      await samplesApi.enterResult(sampleId, sampleTestId, value, enteredBy);
      await load();
    } catch (err) {
      setActionError(errorMessage(err));
    }
  }

  if (loading) return <Spinner />;
  if (error) return <ErrorBanner message={error} />;
  if (!sample) return null;

  const pendingCount = sample.tests.filter((t) => t.status === "PENDING").length;
  const resultsBlocked = sample.status === "REJECTED" || sample.status === "COMPLETED";
  const canStart = sample.status === "RECEIVED";
  const canReject = sample.status === "RECEIVED" || sample.status === "IN_PROGRESS";
  const canComplete = sample.status === "IN_PROGRESS" && pendingCount === 0;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">{sample.barcode}</h1>
          <p className="mt-1 text-sm text-slate-500">
            {sample.customer.name} · Kabul: {formatDateTime(sample.receivedAt)}
          </p>
        </div>
        <div className="flex gap-2">
          {canStart && (
            <button className="btn-secondary" onClick={handleStart} disabled={actionPending}>
              İşleme Al
            </button>
          )}
          {canReject && (
            <button
              className="btn-danger"
              onClick={() => setShowRejectForm((v) => !v)}
              disabled={actionPending}
            >
              Reddet
            </button>
          )}
          {sample.status === "IN_PROGRESS" && (
            <button
              className="btn-primary"
              onClick={handleComplete}
              disabled={actionPending || !canComplete}
              title={!canComplete ? `${pendingCount} test bekliyor` : undefined}
            >
              Tamamla
            </button>
          )}
        </div>
      </div>

      {actionError && <ErrorBanner message={actionError} onDismiss={() => setActionError(null)} />}

      {showRejectForm && (
        <form onSubmit={handleReject} className="card flex items-end gap-3 p-4">
          <div className="flex-1">
            <label className="label">Red gerekçesi</label>
            <input
              className="input"
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="Numune hemolizli, yeniden alınmalı"
              required
            />
          </div>
          <button type="submit" className="btn-danger" disabled={actionPending}>
            Reddi Onayla
          </button>
          <button type="button" className="btn-secondary" onClick={() => setShowRejectForm(false)}>
            Vazgeç
          </button>
        </form>
      )}

      <div className="card p-6">
        <SampleStageStepper status={sample.status} />
      </div>

      {sample.rejectionReason && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          <span className="font-medium">Red gerekçesi:</span> {sample.rejectionReason}
        </div>
      )}

      <div className="card overflow-hidden">
        <div className="border-b border-slate-100 px-5 py-4">
          <h2 className="font-semibold text-slate-900">
            Testler {pendingCount > 0 && <span className="text-slate-400">· {pendingCount} bekliyor</span>}
          </h2>
        </div>
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-5 py-3 font-medium">Test</th>
              <th className="px-5 py-3 font-medium">Referans Aralığı</th>
              <th className="px-5 py-3 font-medium">Sonuç</th>
              <th className="px-5 py-3 font-medium">Giriş</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {sample.tests.map((test) => (
              <ResultEntryRow
                key={test.id}
                test={test}
                disabled={resultsBlocked}
                onSubmit={(value, enteredBy) => handleResultSubmit(test.id, value, enteredBy)}
              />
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <div className="border-b border-slate-100 px-5 py-4">
          <h2 className="font-semibold text-slate-900">Aşama Geçmişi</h2>
        </div>
        <ol className="divide-y divide-slate-100">
          {sample.history.map((entry) => (
            <li key={entry.id} className="flex items-center justify-between px-5 py-3 text-sm">
              <span className="text-slate-700">
                {entry.fromStatus ? `${entry.fromStatus} → ${entry.toStatus}` : `${entry.toStatus} (ilk kayıt)`}
                {entry.note && <span className="text-slate-400"> · {entry.note}</span>}
              </span>
              <span className="text-xs text-slate-400">{formatDateTime(entry.changedAt)}</span>
            </li>
          ))}
        </ol>
      </div>
    </div>
  );
}

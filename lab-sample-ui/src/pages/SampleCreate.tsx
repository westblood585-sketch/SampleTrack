import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { customersApi } from "../api/customers";
import { testDefinitionsApi } from "../api/testDefinitions";
import { samplesApi } from "../api/samples";
import { ErrorBanner } from "../components/ErrorBanner";
import { Spinner } from "../components/Spinner";
import { errorMessage } from "../lib/format";
import type { Customer, TestDefinition } from "../types";

export default function SampleCreate() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [definitions, setDefinitions] = useState<TestDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [barcode, setBarcode] = useState("");
  const [customerId, setCustomerId] = useState<number | "">("");
  const [selectedTests, setSelectedTests] = useState<Set<number>>(new Set());

  useEffect(() => {
    Promise.all([customersApi.list(0, 200), testDefinitionsApi.list(true)])
      .then(([customerPage, defs]) => {
        setCustomers(customerPage.content);
        setDefinitions(defs);
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  function toggleTest(id: number) {
    setSelectedTests((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!customerId || selectedTests.size === 0) return;
    setSubmitting(true);
    setError(null);
    try {
      const created = await samplesApi.create({
        barcode,
        customerId,
        testDefinitionIds: Array.from(selectedTests),
      });
      navigate(`/samples/${created.id}`);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Spinner />;

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Numune Kabul Et</h1>
        <p className="mt-1 text-sm text-slate-500">Barkod, müşteri ve uygulanacak testleri seçin.</p>
      </div>

      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      <form onSubmit={handleSubmit} className="card space-y-5 p-6">
        <div>
          <label className="label">Barkod</label>
          <input
            className="input"
            placeholder="BC-2026-000123"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            required
          />
        </div>

        <div>
          <label className="label">Müşteri</label>
          <select
            className="input"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value ? Number(e.target.value) : "")}
            required
          >
            <option value="">Müşteri seçin…</option>
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="label">Testler ({selectedTests.size} seçildi)</label>
          <div className="grid max-h-72 grid-cols-2 gap-2 overflow-y-auto rounded-lg border border-slate-200 p-3">
            {definitions.map((def) => (
              <label
                key={def.id}
                className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 text-sm ${
                  selectedTests.has(def.id) ? "bg-brand-50 text-brand-700" : "hover:bg-slate-50"
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedTests.has(def.id)}
                  onChange={() => toggleTest(def.id)}
                  className="rounded border-slate-300 text-brand-600 focus:ring-brand-500"
                />
                <span>
                  <span className="font-medium">{def.code}</span> — {def.name}
                </span>
              </label>
            ))}
            {definitions.length === 0 && (
              <p className="col-span-2 py-4 text-center text-sm text-slate-400">
                Aktif test tanımı bulunamadı.
              </p>
            )}
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="submit"
            className="btn-primary"
            disabled={submitting || !customerId || selectedTests.size === 0 || !barcode}
          >
            {submitting ? "Kaydediliyor…" : "Numuneyi Kabul Et"}
          </button>
        </div>
      </form>
    </div>
  );
}

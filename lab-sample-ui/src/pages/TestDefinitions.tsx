import { useEffect, useState } from "react";
import { testDefinitionsApi } from "../api/testDefinitions";
import { ErrorBanner } from "../components/ErrorBanner";
import { Spinner } from "../components/Spinner";
import { Modal } from "../components/Modal";
import { formatNumber, errorMessage } from "../lib/format";
import type { TestDefinition, TestDefinitionInput } from "../types";

const EMPTY_FORM: TestDefinitionInput = { code: "", name: "", unit: "", refMin: undefined, refMax: undefined };

export default function TestDefinitions() {
  const [items, setItems] = useState<TestDefinition[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [editing, setEditing] = useState<TestDefinition | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<TestDefinitionInput>(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    testDefinitionsApi
      .list(false)
      .then(setItems)
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  function openCreate() {
    setEditing(null);
    setForm(EMPTY_FORM);
    setFormError(null);
    setShowForm(true);
  }

  function openEdit(def: TestDefinition) {
    setEditing(def);
    setForm({ code: def.code, name: def.name, unit: def.unit ?? "", refMin: def.refMin ?? undefined, refMax: def.refMax ?? undefined });
    setFormError(null);
    setShowForm(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      if (editing) {
        await testDefinitionsApi.update(editing.id, form);
      } else {
        await testDefinitionsApi.create(form);
      }
      setShowForm(false);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleActive(def: TestDefinition) {
    try {
      await testDefinitionsApi.setActive(def.id, !def.active);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Test Tanımları</h1>
          <p className="mt-1 text-sm text-slate-500">Laboratuvar test kataloğu ve referans aralıkları.</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>
          + Yeni Test Tanımı
        </button>
      </div>

      {error && <ErrorBanner message={error} />}
      {loading && <Spinner />}

      {!loading && (
        <div className="card overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3 font-medium">Kod</th>
                <th className="px-5 py-3 font-medium">Ad</th>
                <th className="px-5 py-3 font-medium">Birim</th>
                <th className="px-5 py-3 font-medium">Referans Aralığı</th>
                <th className="px-5 py-3 font-medium">Durum</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {items.map((def) => (
                <tr key={def.id} className="hover:bg-slate-50">
                  <td className="px-5 py-3 font-medium text-slate-900">{def.code}</td>
                  <td className="px-5 py-3 text-slate-700">{def.name}</td>
                  <td className="px-5 py-3 text-slate-500">{def.unit ?? "—"}</td>
                  <td className="px-5 py-3 text-slate-500">
                    {def.refMin != null && def.refMax != null
                      ? `${formatNumber(def.refMin)}–${formatNumber(def.refMax)}`
                      : "—"}
                  </td>
                  <td className="px-5 py-3">
                    <span className={`badge ${def.active ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-500"}`}>
                      {def.active ? "Aktif" : "Pasif"}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-right">
                    <div className="flex justify-end gap-2">
                      <button className="text-sm font-medium text-brand-600 hover:underline" onClick={() => openEdit(def)}>
                        Düzenle
                      </button>
                      <button
                        className="text-sm font-medium text-slate-500 hover:underline"
                        onClick={() => toggleActive(def)}
                      >
                        {def.active ? "Pasifleştir" : "Aktifleştir"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {items.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-5 py-10 text-center text-slate-400">
                    Henüz test tanımı yok.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <Modal title={editing ? `${editing.code} Düzenle` : "Yeni Test Tanımı"} onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-4">
            {formError && <ErrorBanner message={formError} />}
            <div>
              <label className="label">Kod</label>
              <input
                className="input"
                value={form.code}
                onChange={(e) => setForm({ ...form, code: e.target.value })}
                required
              />
            </div>
            <div>
              <label className="label">Ad</label>
              <input
                className="input"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
              />
            </div>
            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="label">Birim</label>
                <input
                  className="input"
                  value={form.unit ?? ""}
                  onChange={(e) => setForm({ ...form, unit: e.target.value })}
                />
              </div>
              <div>
                <label className="label">Min</label>
                <input
                  className="input"
                  inputMode="decimal"
                  value={form.refMin ?? ""}
                  onChange={(e) => setForm({ ...form, refMin: e.target.value ? Number(e.target.value) : undefined })}
                />
              </div>
              <div>
                <label className="label">Max</label>
                <input
                  className="input"
                  inputMode="decimal"
                  value={form.refMax ?? ""}
                  onChange={(e) => setForm({ ...form, refMax: e.target.value ? Number(e.target.value) : undefined })}
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button type="button" className="btn-secondary" onClick={() => setShowForm(false)}>
                Vazgeç
              </button>
              <button type="submit" className="btn-primary" disabled={submitting}>
                {submitting ? "Kaydediliyor…" : "Kaydet"}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

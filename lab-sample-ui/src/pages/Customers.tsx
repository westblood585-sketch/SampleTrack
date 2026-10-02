import { useEffect, useState } from "react";
import { customersApi } from "../api/customers";
import { ErrorBanner } from "../components/ErrorBanner";
import { Spinner } from "../components/Spinner";
import { Modal } from "../components/Modal";
import { formatDateTime, errorMessage } from "../lib/format";
import type { Customer, CustomerInput } from "../types";

const EMPTY_FORM: CustomerInput = { name: "", email: "", phone: "" };

export default function Customers() {
  const [items, setItems] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [editing, setEditing] = useState<Customer | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<CustomerInput>(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    customersApi
      .list(0, 100)
      .then((page) => setItems(page.content))
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

  function openEdit(customer: Customer) {
    setEditing(customer);
    setForm({ name: customer.name, email: customer.email, phone: customer.phone ?? "" });
    setFormError(null);
    setShowForm(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      if (editing) await customersApi.update(editing.id, form);
      else await customersApi.create(form);
      setShowForm(false);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Müşteriler</h1>
          <p className="mt-1 text-sm text-slate-500">Numune gönderen klinik ve hastaneler.</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>
          + Yeni Müşteri
        </button>
      </div>

      {error && <ErrorBanner message={error} />}
      {loading && <Spinner />}

      {!loading && (
        <div className="card overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3 font-medium">Ad</th>
                <th className="px-5 py-3 font-medium">E-posta</th>
                <th className="px-5 py-3 font-medium">Telefon</th>
                <th className="px-5 py-3 font-medium">Kayıt Tarihi</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {items.map((c) => (
                <tr key={c.id} className="hover:bg-slate-50">
                  <td className="px-5 py-3 font-medium text-slate-900">{c.name}</td>
                  <td className="px-5 py-3 text-slate-600">{c.email}</td>
                  <td className="px-5 py-3 text-slate-500">{c.phone ?? "—"}</td>
                  <td className="px-5 py-3 text-slate-500">{formatDateTime(c.createdAt)}</td>
                  <td className="px-5 py-3 text-right">
                    <button className="text-sm font-medium text-brand-600 hover:underline" onClick={() => openEdit(c)}>
                      Düzenle
                    </button>
                  </td>
                </tr>
              ))}
              {items.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-5 py-10 text-center text-slate-400">
                    Henüz müşteri yok.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <Modal title={editing ? "Müşteriyi Düzenle" : "Yeni Müşteri"} onClose={() => setShowForm(false)}>
          <form onSubmit={handleSubmit} className="space-y-4">
            {formError && <ErrorBanner message={formError} />}
            <div>
              <label className="label">Ad</label>
              <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div>
              <label className="label">E-posta</label>
              <input
                className="input"
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            <div>
              <label className="label">Telefon</label>
              <input className="input" value={form.phone ?? ""} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
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

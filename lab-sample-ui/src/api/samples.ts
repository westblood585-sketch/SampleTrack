import { api } from "../lib/apiClient";
import type { Page, SampleDetail, SampleStatus, SampleSummary, SampleTest } from "../types";

export interface SampleCreateInput {
  barcode: string;
  customerId: number;
  testDefinitionIds: number[];
}

export interface SampleSearchParams {
  status?: SampleStatus | "";
  customerId?: number;
  barcode?: string;
  page?: number;
  size?: number;
}

export const samplesApi = {
  search: ({ status, customerId, barcode, page = 0, size = 20 }: SampleSearchParams) => {
    const params = new URLSearchParams({ page: String(page), size: String(size), sort: "receivedAt,desc" });
    if (status) params.set("status", status);
    if (customerId) params.set("customerId", String(customerId));
    if (barcode) params.set("barcode", barcode);
    return api.get<Page<SampleSummary>>(`/api/samples?${params.toString()}`);
  },
  get: (id: number) => api.get<SampleDetail>(`/api/samples/${id}`),
  create: (input: SampleCreateInput) => api.post<SampleDetail>("/api/samples", input),
  start: (id: number) => api.post<SampleDetail>(`/api/samples/${id}/start`),
  reject: (id: number, reason: string) => api.post<SampleDetail>(`/api/samples/${id}/reject`, { reason }),
  complete: (id: number) => api.post<SampleDetail>(`/api/samples/${id}/complete`),
  enterResult: (sampleId: number, sampleTestId: number, value: number, enteredBy: string) =>
    api.post<SampleTest>(`/api/samples/${sampleId}/tests/${sampleTestId}/result`, { value, enteredBy }),
};

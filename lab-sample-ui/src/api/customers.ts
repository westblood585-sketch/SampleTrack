import { api } from "../lib/apiClient";
import type { Customer, CustomerInput, Page } from "../types";

export const customersApi = {
  list: (page = 0, size = 50) =>
    api.get<Page<Customer>>(`/api/customers?page=${page}&size=${size}&sort=name,asc`),
  get: (id: number) => api.get<Customer>(`/api/customers/${id}`),
  create: (input: CustomerInput) => api.post<Customer>("/api/customers", input),
  update: (id: number, input: CustomerInput) => api.put<Customer>(`/api/customers/${id}`, input),
};

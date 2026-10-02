import { api } from "../lib/apiClient";
import type { TestDefinition, TestDefinitionInput } from "../types";

export const testDefinitionsApi = {
  list: (onlyActive = false) =>
    api.get<TestDefinition[]>(`/api/test-definitions?onlyActive=${onlyActive}`),
  get: (id: number) => api.get<TestDefinition>(`/api/test-definitions/${id}`),
  create: (input: TestDefinitionInput) => api.post<TestDefinition>("/api/test-definitions", input),
  update: (id: number, input: TestDefinitionInput) =>
    api.put<TestDefinition>(`/api/test-definitions/${id}`, input),
  setActive: (id: number, active: boolean) =>
    api.patch<TestDefinition>(`/api/test-definitions/${id}/active?active=${active}`),
};

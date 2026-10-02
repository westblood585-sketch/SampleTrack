export type SampleStatus = "RECEIVED" | "IN_PROGRESS" | "COMPLETED" | "REJECTED";
export type SampleTestStatus = "PENDING" | "COMPLETED";
export type ResultFlag = "NORMAL" | "LOW" | "HIGH" | "NOT_APPLICABLE";

export interface Customer {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  createdAt: string;
}

export interface CustomerInput {
  name: string;
  email: string;
  phone?: string;
}

export interface TestDefinition {
  id: number;
  code: string;
  name: string;
  unit: string | null;
  refMin: number | null;
  refMax: number | null;
  active: boolean;
}

export interface TestDefinitionInput {
  code: string;
  name: string;
  unit?: string;
  refMin?: number | null;
  refMax?: number | null;
}

export interface TestResult {
  id: number;
  value: number;
  enteredAt: string;
  enteredBy: string;
  flag: ResultFlag;
}

export interface SampleTest {
  id: number;
  testDefinitionId: number;
  code: string;
  name: string;
  unit: string | null;
  refMin: number | null;
  refMax: number | null;
  status: SampleTestStatus;
  result: TestResult | null;
}

export interface StageHistory {
  id: number;
  fromStatus: SampleStatus | null;
  toStatus: SampleStatus;
  changedAt: string;
  note: string | null;
}

export interface SampleSummary {
  id: number;
  barcode: string;
  customerId: number;
  customerName: string;
  status: SampleStatus;
  receivedAt: string;
}

export interface SampleDetail {
  id: number;
  barcode: string;
  customer: Customer;
  status: SampleStatus;
  receivedAt: string;
  rejectionReason: string | null;
  tests: SampleTest[];
  history: StageHistory[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  fieldErrors: { field: string; message: string }[];
}

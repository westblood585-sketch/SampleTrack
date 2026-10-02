export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("tr-TR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat("tr-TR", { maximumFractionDigits: 4 }).format(value);
}

export function errorMessage(err: unknown): string {
  if (err && typeof err === "object" && "apiError" in err) {
    return (err as { apiError: { message: string } }).apiError.message;
  }
  if (err instanceof Error) return err.message;
  return "Beklenmeyen bir hata oluştu";
}

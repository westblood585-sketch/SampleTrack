export function ErrorBanner({ message, onDismiss }: { message: string; onDismiss?: () => void }) {
  return (
    <div className="flex items-start justify-between gap-3 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
      <span>{message}</span>
      {onDismiss && (
        <button onClick={onDismiss} className="text-rose-500 hover:text-rose-700" aria-label="Kapat">
          ✕
        </button>
      )}
    </div>
  );
}

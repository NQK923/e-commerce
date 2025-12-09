export const formatCurrency = (value: number, currency = "USD") =>
  new Intl.NumberFormat(undefined, { style: "currency", currency }).format(value);

export const formatDate = (value: string) =>
  new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));

export const formatNumber = (value?: number) =>
  new Intl.NumberFormat(undefined).format(value ?? 0);

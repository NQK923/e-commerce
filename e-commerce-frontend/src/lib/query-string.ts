export const buildQueryString = (params: Record<string, string | number | boolean | undefined>) => {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== "");
  if (!entries.length) return "";
  const usp = new URLSearchParams();
  for (const [key, value] of entries) {
    usp.append(key, String(value));
  }
  return `?${usp.toString()}`;
};

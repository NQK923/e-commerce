import {config} from "../config/env";

export type HttpMethod = "GET" | "POST" | "PUT" | "PATCH" | "DELETE";

type TokenProvider = () => Promise<string | null> | string | null;
type RefreshHandler = () => Promise<boolean>;
type LogoutHandler = () => void;

let tokenProvider: TokenProvider = () => null;
let refreshHandler: RefreshHandler | null = null;
let logoutHandler: LogoutHandler | null = null;

export const setAuthTokenProvider = (provider: TokenProvider) => {
  tokenProvider = provider;
};

export const setRefreshHandler = (handler: RefreshHandler) => {
  refreshHandler = handler;
};

export const setLogoutHandler = (handler: LogoutHandler) => {
  logoutHandler = handler;
};

export class ApiError extends Error {
  status: number;
  data?: unknown;

  constructor(status: number, message: string, data?: unknown) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

type RequestOptions = {
  method?: HttpMethod;
  headers?: Record<string, string>;
  body?: unknown;
  retry?: boolean;
  signal?: AbortSignal;
};

const buildHeaders = async (
  headers: Record<string, string> = {},
): Promise<Record<string, string>> => {
  const authToken = await tokenProvider();
  const finalHeaders: Record<string, string> = {
    Accept: "application/json",
    "Content-Type": "application/json",
    ...headers,
  };

  if (authToken) {
    finalHeaders.Authorization = `Bearer ${authToken}`;
  }

  return finalHeaders;
};

export const apiRequest = async <T>(
  path: string,
  { method = "GET", headers, body, retry = true, signal }: RequestOptions = {},
): Promise<T> => {
  const url = `${config.apiBaseUrl}${path}`;
  const response = await fetch(url, {
    method,
    headers: await buildHeaders(headers),
    body: body ? JSON.stringify(body) : undefined,
    credentials: "include",
    signal,
  });

  if (response.ok) {
    if (response.status === 204) return undefined as T;
      return (await response.json()) as T;
  }

  // Attempt refresh on 401 once.
  if (response.status === 401 && retry && refreshHandler) {
    const refreshed = await refreshHandler();
    if (refreshed) {
      return apiRequest<T>(path, { method, headers, body, retry: false, signal });
    }
    logoutHandler?.();
  }

  let errorMessage = `Request failed with status ${response.status}`;
  let data: unknown;
  try {
    data = await response.json();
    const maybeMessage = (data as { message?: string })?.message;
    if (maybeMessage) {
      errorMessage = maybeMessage;
    }
  } catch {
    // Ignore JSON parse failure and fall back to default message.
  }

  if (response.status === 401 && logoutHandler) {
    logoutHandler();
  }

  throw new ApiError(response.status, errorMessage, data);
};

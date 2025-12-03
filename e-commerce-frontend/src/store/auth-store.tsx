'use client';

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { authApi } from "../api/authApi";
import { setAuthTokenProvider, setLogoutHandler, setRefreshHandler } from "../lib/api-client";
import { AuthResponse, AuthTokens, LoginRequest, RegisterRequest, User } from "../types/auth";

type AuthContextValue = {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  initializing: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<boolean>;
  setSessionFromOAuth: (payload: AuthResponse | (AuthTokens & { user?: User })) => Promise<void>;
  setUserProfile: (user: User) => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const ACCESS_KEY = "ecommerce_access_token";
const REFRESH_KEY = "ecommerce_refresh_token";

const getStoredAccessToken = () => (typeof window === "undefined" ? null : localStorage.getItem(ACCESS_KEY));
const getStoredRefreshToken = () => (typeof window === "undefined" ? null : localStorage.getItem(REFRESH_KEY));

const persistTokens = (tokens: AuthTokens | null) => {
  if (typeof window === "undefined") return;
  if (tokens) {
    localStorage.setItem(ACCESS_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
  } else {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [refreshToken, setRefreshToken] = useState<string | null>(null);
  const [initializing, setInitializing] = useState(true);

  const resolveRefreshToken = useCallback(() => refreshToken ?? getStoredRefreshToken(), [refreshToken]);

  const syncTokens = useCallback((tokens: AuthTokens | null) => {
    setAccessToken(tokens?.accessToken ?? null);
    setRefreshToken(tokens?.refreshToken ?? null);
    persistTokens(tokens);
  }, []);

  const logout = useCallback(async () => {
    const tokenToRevoke = resolveRefreshToken();
    if (tokenToRevoke) {
      try {
        await authApi.logout(tokenToRevoke);
      } catch {
        // Ignore logout failures to avoid blocking user.
      }
    }
    syncTokens(null);
    setUser(null);
  }, [resolveRefreshToken, syncTokens]);

  const refresh = useCallback(async () => {
    const tokenToRefresh = resolveRefreshToken();
    if (!tokenToRefresh) return false;
    try {
      const tokens = await authApi.refresh(tokenToRefresh);
      syncTokens(tokens);
      return true;
    } catch {
      await logout();
      return false;
    }
  }, [logout, refreshToken, syncTokens]);

  const fetchUser = useCallback(async () => {
    try {
      const me = await authApi.me();
      setUser(me);
    } catch {
      setUser(null);
    }
  }, []);

  const handleAuthResponse = useCallback(
    async (auth: AuthResponse | (AuthTokens & { user?: User })) => {
      syncTokens({ accessToken: auth.accessToken, refreshToken: auth.refreshToken });
      if ("user" in auth && auth.user) {
        setUser(auth.user);
      } else {
        await fetchUser();
      }
    },
    [fetchUser, syncTokens],
  );

  const login = useCallback(
    async (payload: LoginRequest) => {
      const response = await authApi.login(payload);
      await handleAuthResponse(response);
    },
    [handleAuthResponse],
  );

  const register = useCallback(
    async (payload: RegisterRequest) => {
      const response = await authApi.register(payload);
      await handleAuthResponse(response);
    },
    [handleAuthResponse],
  );

  const updateUserProfile = useCallback((profile: User) => setUser(profile), []);

  useEffect(() => {
    // Configure API client hooks for auth.
    setAuthTokenProvider(() => accessToken ?? getStoredAccessToken());
    setRefreshHandler(refresh);
    setLogoutHandler(logout);
  }, [accessToken, logout, refresh]);

  useEffect(() => {
    const bootstrap = async () => {
      if (typeof window === "undefined") return;
      const storedAccess = getStoredAccessToken();
      const storedRefresh = getStoredRefreshToken();
      if (storedAccess && storedRefresh) {
        syncTokens({ accessToken: storedAccess, refreshToken: storedRefresh });
        await fetchUser();
      }
      setInitializing(false);
    };
    void bootstrap();
  }, [fetchUser, syncTokens]);

  const value = useMemo(
    () => ({
      user,
      accessToken,
      refreshToken,
      initializing,
      login,
      register,
      logout,
      refresh,
      setSessionFromOAuth: handleAuthResponse,
      setUserProfile: updateUserProfile,
    }),
    [
      accessToken,
      handleAuthResponse,
      initializing,
      login,
      logout,
      refresh,
      refreshToken,
      register,
      updateUserProfile,
      user,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};

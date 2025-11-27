export type AuthProvider = "LOCAL" | "GOOGLE" | "FACEBOOK";

export type User = {
  id: string;
  email: string;
  displayName: string;
  roles: string[];
  provider: AuthProvider;
  avatarUrl?: string;
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  email: string;
  password: string;
  displayName: string;
};

export type AuthResponse = {
  user: User;
  accessToken: string;
  refreshToken: string;
};

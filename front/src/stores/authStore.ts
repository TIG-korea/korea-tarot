import { create } from "zustand";

import {
  getProfile,
  login,
  logout,
  refreshToken,
  signup,
  type AuthUser,
  type LoginRequest,
  type ProfileResponse,
  type SignupRequest
} from "@/api/auth";
import { setApiAccessToken } from "@/api/client";

interface AuthState {
  user: AuthUser | null;
  profile: ProfileResponse | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setSession: (user: AuthUser, accessToken: string) => void;
  clearSession: () => void;
  signup: (request: SignupRequest) => Promise<void>;
  login: (request: LoginRequest) => Promise<void>;
  refresh: () => Promise<void>;
  loadProfile: () => Promise<void>;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  profile: null,
  accessToken: null,
  isAuthenticated: false,
  isLoading: false,

  setSession: (user, accessToken) => {
    setApiAccessToken(accessToken);
    set({
      user,
      accessToken,
      isAuthenticated: true
    });
  },

  clearSession: () => {
    setApiAccessToken(null);
    set({
      user: null,
      profile: null,
      accessToken: null,
      isAuthenticated: false
    });
  },

  signup: async (request) => {
    set({ isLoading: true });
    try {
      const response = await signup(request);
      get().setSession(
        {
          id: response.userId,
          email: response.email,
          nickname: response.nickname
        },
        response.accessToken
      );
    } finally {
      set({ isLoading: false });
    }
  },

  login: async (request) => {
    set({ isLoading: true });
    try {
      const response = await login(request);
      get().setSession(response.user, response.accessToken);
    } finally {
      set({ isLoading: false });
    }
  },

  refresh: async () => {
    const response = await refreshToken();
    setApiAccessToken(response.accessToken);
    set({
      accessToken: response.accessToken,
      isAuthenticated: true
    });
  },

  loadProfile: async () => {
    set({ isLoading: true });
    try {
      const profile = await getProfile();
      set({
        profile,
        user: {
          id: profile.id,
          email: profile.email,
          nickname: profile.nickname
        },
        isAuthenticated: true
      });
    } finally {
      set({ isLoading: false });
    }
  },

  logout: async () => {
    try {
      await logout();
    } finally {
      get().clearSession();
    }
  }
}));

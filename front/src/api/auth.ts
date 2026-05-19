import { apiClient } from "@/api/client";

export type UserStatus = "ACTIVE" | "WITHDRAWAL_REQUESTED" | "WITHDRAWN";

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: null;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
  termsAgreed: boolean;
  privacyAgreed: boolean;
  marketingAgreed: boolean;
}

export interface SignupResponse {
  userId: number;
  email: string;
  nickname: string;
  accessToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUser {
  id: number;
  email: string;
  nickname: string;
}

export interface LoginResponse {
  accessToken: string;
  user: AuthUser;
}

export interface RefreshResponse {
  accessToken: string;
}

export interface ProfileResponse {
  id: number;
  email: string;
  nickname: string;
  status: UserStatus;
  createdAt: string;
}

export async function signup(request: SignupRequest) {
  const response = await apiClient.post<ApiResponse<SignupResponse>>("/auth/signup", request);
  return response.data.data;
}

export async function login(request: LoginRequest) {
  const response = await apiClient.post<ApiResponse<LoginResponse>>("/auth/login", request);
  return response.data.data;
}

export async function refreshToken() {
  const response = await apiClient.post<ApiResponse<RefreshResponse>>("/auth/refresh");
  return response.data.data;
}

export async function logout() {
  await apiClient.post<ApiResponse<null>>("/auth/logout");
}

export async function getProfile() {
  const response = await apiClient.get<ApiResponse<ProfileResponse>>("/users/me");
  return response.data.data;
}

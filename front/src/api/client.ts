import axios, { AxiosHeaders } from "axios";

let accessToken: string | null = null;

export function setApiAccessToken(token: string | null) {
  accessToken = token;
}

export function getApiAccessToken() {
  return accessToken;
}

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1",
  withCredentials: true
});

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers = AxiosHeaders.from(config.headers);
    config.headers.set("Authorization", `Bearer ${accessToken}`);
  }

  return config;
});

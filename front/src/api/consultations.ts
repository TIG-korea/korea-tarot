import { apiClient } from "@/api/client";
import type { ApiResponse } from "@/api/auth";

export interface CreateDraftRequest {
  concern: string;
}

export interface CreateDraftResponse {
  draftId: string;
  deckSize: number;
  expiresAt: string;
}

export async function createDraft(request: CreateDraftRequest) {
  const response = await apiClient.post<ApiResponse<CreateDraftResponse>>("/consultations/draft", request);
  return response.data.data;
}

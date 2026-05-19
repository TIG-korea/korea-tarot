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

export type PositionCode = "PRESENT" | "OBSTACLE" | "ADVICE";

export interface CardSelectionRequest {
  deckIndex: number;
  positionCode: PositionCode;
}

export interface CreateConsultationRequest {
  draftId: string;
  selections: CardSelectionRequest[];
}

export interface CreateConsultationResponse {
  consultationId: number;
  status: "PENDING" | "STREAMING" | "COMPLETED" | "FAILED" | "CANCELLED";
  streamUrl: string;
}

export async function createDraft(request: CreateDraftRequest) {
  const response = await apiClient.post<ApiResponse<CreateDraftResponse>>("/consultations/draft", request);
  return response.data.data;
}

export async function createConsultation(request: CreateConsultationRequest, idempotencyKey: string) {
  const response = await apiClient.post<ApiResponse<CreateConsultationResponse>>("/consultations", request, {
    headers: {
      "Idempotency-Key": idempotencyKey
    }
  });
  return response.data.data;
}

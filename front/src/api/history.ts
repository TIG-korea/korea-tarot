import { apiClient } from "@/api/client";
import type { ApiResponse } from "@/api/auth";
import type { ConsultationDetailResponse } from "@/api/consultations";

export interface ConsultationHistoryItem {
  consultationId: number;
  concernPreview: string;
  cardNames: string[];
  categoryCode: string | null;
  summary: string | null;
  status: "PENDING" | "STREAMING" | "COMPLETED" | "FAILED" | "CANCELLED";
  createdAt: string;
}

export interface ConsultationHistoryListResponse {
  items: ConsultationHistoryItem[];
  nextCursor: string | null;
}

export async function getConsultationHistory(cursor?: string, size = 20) {
  const response = await apiClient.get<ApiResponse<ConsultationHistoryListResponse>>("/consultations", {
    params: {
      cursor,
      size
    }
  });
  return response.data.data;
}

export async function getHistoryDetail(consultationId: number) {
  const response = await apiClient.get<ApiResponse<ConsultationDetailResponse>>(`/consultations/${consultationId}`);
  return response.data.data;
}

export async function deleteHistory(consultationId: number) {
  await apiClient.delete<ApiResponse<null>>(`/consultations/${consultationId}`);
}

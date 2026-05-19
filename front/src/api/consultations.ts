import { apiClient } from "@/api/client";
import { getApiAccessToken } from "@/api/client";
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

export interface ConsultationMetaCard {
  cardId: number;
  cardName: string;
  positionCode: PositionCode;
}

export interface ConsultationMetaEvent {
  consultationId: number;
  cards: ConsultationMetaCard[];
}

export interface InterpretationCardResult {
  positionCode: PositionCode;
  positionName: string;
  cardId: number;
  cardName: string;
  interpretation: string;
}

export interface InterpretationResult {
  summary: string;
  overall: string;
  cards: InterpretationCardResult[];
  advice: string;
  caution: string;
}

export interface ConsultationStreamDoneEvent {
  result: InterpretationResult;
}

export interface ConsultationStreamErrorEvent {
  code: string;
  message: string;
}

export interface ConsultationDetailCard {
  cardId: number;
  cardNameEn: string;
  cardNameKo: string;
  positionOrder: number;
  positionCode: PositionCode;
  positionName: string;
  orientation: "UPRIGHT";
  imageUrl?: string | null;
}

export interface ConsultationDetailResponse {
  consultationId: number;
  concern: string;
  spreadType: "THREE_CARD";
  categoryCode: string;
  status: "PENDING" | "STREAMING" | "COMPLETED" | "FAILED" | "CANCELLED";
  cards: ConsultationDetailCard[];
  result: InterpretationResult | null;
  createdAt: string;
}

export interface ConsultationStreamHandlers {
  onMeta?: (event: ConsultationMetaEvent) => void;
  onToken?: (text: string) => void;
  onDone?: (event: ConsultationStreamDoneEvent) => void;
  onError?: (event: ConsultationStreamErrorEvent) => void;
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

export async function getConsultation(consultationId: number) {
  const response = await apiClient.get<ApiResponse<ConsultationDetailResponse>>(`/consultations/${consultationId}`);
  return response.data.data;
}

export async function streamConsultationEvents(
  consultationId: number,
  handlers: ConsultationStreamHandlers
) {
  const response = await fetch(buildApiUrl(`/consultations/${consultationId}/events`), {
    credentials: "include",
    headers: buildStreamHeaders()
  });

  if (!response.ok || !response.body) {
    throw new Error("상담 스트림 연결에 실패했습니다.");
  }

  await readSseStream(response.body, handlers);
}

function buildApiUrl(path: string) {
  const baseUrl = apiClient.defaults.baseURL ?? "";
  return `${baseUrl.replace(/\/$/, "")}${path}`;
}

function buildStreamHeaders() {
  const headers = new Headers({ Accept: "text/event-stream" });
  const token = getApiAccessToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  return headers;
}

async function readSseStream(
  body: ReadableStream<Uint8Array>,
  handlers: ConsultationStreamHandlers
) {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const blocks = buffer.split(/\r?\n\r?\n/);
    buffer = blocks.pop() ?? "";

    for (const block of blocks) {
      dispatchSseBlock(block, handlers);
    }
  }

  if (buffer.trim()) {
    dispatchSseBlock(buffer, handlers);
  }
}

function dispatchSseBlock(block: string, handlers: ConsultationStreamHandlers) {
  const parsed = parseSseBlock(block);
  if (!parsed.event || !parsed.data) {
    return;
  }

  if (parsed.event === "meta") {
    handlers.onMeta?.(parsed.data as ConsultationMetaEvent);
    return;
  }

  if (parsed.event === "token") {
    const payload = parsed.data as { text?: string };
    handlers.onToken?.(payload.text ?? "");
    return;
  }

  if (parsed.event === "done") {
    handlers.onDone?.(normalizeDonePayload(parsed.data));
    return;
  }

  if (parsed.event === "error") {
    handlers.onError?.(parsed.data as ConsultationStreamErrorEvent);
  }
}

function parseSseBlock(block: string) {
  let event = "";
  const dataLines: string[] = [];

  for (const line of block.split(/\r?\n/)) {
    if (line.startsWith("event:")) {
      event = line.slice("event:".length).trim();
    }
    if (line.startsWith("data:")) {
      dataLines.push(line.slice("data:".length).trimStart());
    }
  }

  const dataText = dataLines.join("\n");
  return {
    event,
    data: dataText ? JSON.parse(dataText) : null
  };
}

function normalizeDonePayload(payload: unknown): ConsultationStreamDoneEvent {
  if (isObject(payload) && "result" in payload) {
    return {
      result: payload.result as InterpretationResult
    };
  }
  return { result: payload as InterpretationResult };
}

function isObject(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

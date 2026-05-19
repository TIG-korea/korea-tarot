import { CardSelectionFlow } from "@/features/consultations/CardSelectionFlow";

type SearchParams = Promise<Record<string, string | string[] | undefined>>;

interface SelectPageProps {
  searchParams?: SearchParams;
}

export default async function SelectPage({ searchParams }: SelectPageProps) {
  const params = await searchParams;
  const draftId = firstValue(params?.draftId);
  const deckSize = Number(firstValue(params?.deckSize) || "22");
  const expiresAt = firstValue(params?.expiresAt);

  return (
    <CardSelectionFlow
      deckSize={Number.isFinite(deckSize) && deckSize > 0 ? deckSize : 22}
      draftId={draftId}
      expiresAt={expiresAt}
    />
  );
}

function firstValue(value: string | string[] | undefined) {
  if (Array.isArray(value)) {
    return value[0] ?? "";
  }
  return value ?? "";
}

import { NewConsultationForm } from "@/features/consultations/NewConsultationForm";

type SearchParams = Promise<Record<string, string | string[] | undefined>>;

interface NewConsultationPageProps {
  searchParams?: SearchParams;
}

export default async function NewConsultationPage({ searchParams }: NewConsultationPageProps) {
  const params = await searchParams;
  const concern = firstValue(params?.concern);

  return <NewConsultationForm initialConcern={concern} />;
}

function firstValue(value: string | string[] | undefined) {
  if (Array.isArray(value)) {
    return value[0] ?? "";
  }
  return value ?? "";
}

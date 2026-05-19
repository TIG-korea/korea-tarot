from fastapi import APIRouter

from app.api import interpretations

router = APIRouter()
router.include_router(interpretations.router)


@router.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}

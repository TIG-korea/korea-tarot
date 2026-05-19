from fastapi import FastAPI

from app.api.router import router
from app.core.logging import configure_logging


def create_app() -> FastAPI:
    configure_logging()
    app = FastAPI(title="Korea Tarot AI Server", version="0.0.1")
    app.include_router(router)
    return app


app = create_app()


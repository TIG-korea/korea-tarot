from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "korea-tarot-ai-server"
    environment: str = "local"
    llm_provider: str = "mock"
    llm_api_key: str | None = None
    backend_base_url: str = "http://localhost:8080"
    backend_card_docs_path: str = "/internal/v1/card-interpretations/lookup"
    backend_timeout_seconds: float = 5.0

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )


settings = Settings()

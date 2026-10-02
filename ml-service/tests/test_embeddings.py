import os

import pytest
from fastapi import HTTPException

from app import main
from app.embeddings import FakeEmbeddingProvider


def test_fake_provider_is_deterministic():
    provider = FakeEmbeddingProvider()
    assert provider.embed(["same text"])[0] == provider.embed(["same text"])[0]
    assert provider.embed(["same text"])[0] != provider.embed(["different text"])[0]


def test_health_before_and_after_fake_embedding(monkeypatch):
    provider = FakeEmbeddingProvider()
    monkeypatch.setattr(main, "embedding_provider", provider)
    assert main.health()["modelLoaded"] is False
    main.embed_texts(["hello"])
    assert main.health()["modelLoaded"] is True


def test_embedding_request_limits(monkeypatch):
    monkeypatch.setattr(main, "MAX_EMBED_TEXTS", 2)
    monkeypatch.setattr(main, "MAX_EMBED_TEXT_LENGTH", 5)
    with pytest.raises(HTTPException) as batch_error:
        main.embed_texts(["one", "two", "three"])
    assert batch_error.value.status_code == 413
    with pytest.raises(HTTPException) as length_error:
        main.embed_texts(["too long"])
    assert length_error.value.status_code == 413


@pytest.mark.skipif(os.getenv("RUN_FASTEMBED_SMOKE") != "true", reason="real model smoke test is opt-in")
def test_fastembed_smoke(monkeypatch):
    from app.embeddings import FastEmbedEmbeddingProvider
    provider = FastEmbedEmbeddingProvider("BAAI/bge-small-en-v1.5", os.getenv("EMBEDDING_CACHE_DIR", ".cache/fastembed"))
    vector = provider.embed(["a lightweight smoke test"])[0]
    assert vector and provider.model_loaded

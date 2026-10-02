import pytest
from fastapi import HTTPException

from app import main
from app.embeddings import FakeEmbeddingProvider


def test_similarity_uses_fake_embeddings_and_returns_bounded_scores(monkeypatch):
    monkeypatch.setattr(main, "embedding_provider", FakeEmbeddingProvider())
    result = main.similarity({"candidateText": "python backend", "jobs": [
        {"id": "same", "text": "python backend"},
        {"id": "other", "text": "gardening"},
    ]})
    assert result["matches"][0]["jobId"] == "same"
    assert result["matches"][0]["similarity"] == 1.0
    assert all(0 <= item["similarity"] <= 1 for item in result["matches"])


def test_similarity_batch_limit(monkeypatch):
    monkeypatch.setattr(main, "MAX_SIMILARITY_JOBS", 1)
    with pytest.raises(HTTPException) as error:
        main.similarity({"candidateText": "text", "jobs": [{"id": 1, "text": "a"}, {"id": 2, "text": "b"}]})
    assert error.value.status_code == 413

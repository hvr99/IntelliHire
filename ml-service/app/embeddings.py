from abc import ABC, abstractmethod
import hashlib
import math
from pathlib import Path


class EmbeddingProvider(ABC):
    @property
    @abstractmethod
    def model_loaded(self) -> bool:
        raise NotImplementedError

    @abstractmethod
    def embed(self, texts: list[str]) -> list[list[float]]:
        raise NotImplementedError


class FakeEmbeddingProvider(EmbeddingProvider):
    """Small deterministic provider for tests and isolated development."""

    def __init__(self, dimensions: int = 8):
        self.dimensions = dimensions
        self._used = False

    @property
    def model_loaded(self) -> bool:
        return self._used

    def embed(self, texts: list[str]) -> list[list[float]]:
        self._used = True
        vectors = []
        for text in texts:
            digest = hashlib.sha256(text.encode("utf-8")).digest()
            values = [(digest[index] / 127.5) - 1.0 for index in range(self.dimensions)]
            magnitude = math.sqrt(sum(value * value for value in values)) or 1.0
            vectors.append([round(value / magnitude, 8) for value in values])
        return vectors


class FastEmbedEmbeddingProvider(EmbeddingProvider):
    def __init__(self, model_name: str, cache_dir: str | None = None):
        self.model_name = model_name
        self.cache_dir = Path(cache_dir) if cache_dir else None
        self._model = None

    @property
    def model_loaded(self) -> bool:
        return self._model is not None

    def embed(self, texts: list[str]) -> list[list[float]]:
        if self._model is None:
            from fastembed import TextEmbedding
            kwargs = {"model_name": self.model_name}
            if self.cache_dir:
                self.cache_dir.mkdir(parents=True, exist_ok=True)
                kwargs["cache_dir"] = str(self.cache_dir)
            self._model = TextEmbedding(**kwargs)
        return [vector.tolist() if hasattr(vector, "tolist") else list(vector) for vector in self._model.embed(texts)]

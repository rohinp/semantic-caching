from __future__ import annotations

from collections.abc import Mapping, Sequence
from dataclasses import dataclass, field


@dataclass(frozen=True)
class DeterministicEmbeddingService:
    embeddings: Mapping[str, Sequence[float]]

    def embed(self, query: str) -> Sequence[float]:
        return self.embeddings[query]


@dataclass
class RecordingLlmService:
    response_prefix: str = "generated"
    calls: list[str] = field(default_factory=list)

    def generate(self, query: str) -> str:
        self.calls.append(query)
        return f"{self.response_prefix}: {query}"


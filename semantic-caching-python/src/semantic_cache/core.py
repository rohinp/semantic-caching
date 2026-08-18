from __future__ import annotations

from dataclasses import dataclass
import math
from typing import Protocol, Sequence


type Embedding = tuple[float, ...]


class InvalidEmbeddingError(ValueError):
    """Raised when an embedding cannot participate in cosine similarity."""


class EmbeddingService(Protocol):
    def embed(self, query: str) -> Sequence[float]: ...


class LlmService(Protocol):
    def generate(self, query: str) -> str: ...


@dataclass(frozen=True)
class CacheNode:
    identifier: int
    query: str
    response: str
    embedding: Embedding


def _validated_embedding(values: Sequence[float]) -> Embedding:
    if not values:
        raise InvalidEmbeddingError("embedding must not be empty")

    embedding = tuple(float(value) for value in values)
    if any(not math.isfinite(value) for value in embedding):
        raise InvalidEmbeddingError("embedding components must be finite")
    if any(value < 0.0 for value in embedding):
        raise InvalidEmbeddingError("embedding components must be non-negative")
    if math.hypot(*embedding) == 0.0:
        raise InvalidEmbeddingError("embedding must not be a zero vector")
    return embedding


def cosine_similarity(left: Sequence[float], right: Sequence[float]) -> float:
    """Return stable cosine similarity for compatible non-negative vectors."""
    validated_left = _validated_embedding(left)
    validated_right = _validated_embedding(right)
    if len(validated_left) != len(validated_right):
        raise InvalidEmbeddingError("embeddings must have equal dimensions")

    left_norm = math.hypot(*validated_left)
    right_norm = math.hypot(*validated_right)
    similarity = math.fsum(
        (left_value / left_norm) * (right_value / right_norm)
        for left_value, right_value in zip(validated_left, validated_right, strict=True)
    )
    if not math.isfinite(similarity):
        raise ArithmeticError("cosine similarity was not finite")
    if similarity < 0.0:
        if similarity >= -1e-15:
            return 0.0
        raise ArithmeticError("cosine similarity was below zero")
    if similarity > 1.0:
        if similarity <= 1.0 + 1e-15:
            return 1.0
        raise ArithmeticError("cosine similarity exceeded one")
    return similarity


class SemanticCache:
    """Deterministic in-memory semantic cache backed by a weighted graph.

    Equal hit similarities are resolved in favor of the smaller stable node ID.
    Recent search starts are considered newest first.
    """

    def __init__(
        self,
        embedding_service: EmbeddingService,
        llm_service: LlmService,
        *,
        edge_threshold: float,
        hit_threshold: float,
        search_starts: int,
        neighbors_per_start: int,
    ) -> None:
        if not math.isfinite(edge_threshold) or not math.isfinite(hit_threshold):
            raise ValueError("thresholds must be finite")
        if not 0.0 <= edge_threshold < hit_threshold <= 1.0:
            raise ValueError(
                "thresholds must satisfy 0.0 <= edge_threshold < hit_threshold <= 1.0"
            )
        if search_starts <= 0:
            raise ValueError("search_starts must be positive")
        if neighbors_per_start <= 0:
            raise ValueError("neighbors_per_start must be positive")

        self._embedding_service = embedding_service
        self._llm_service = llm_service
        self.edge_threshold = edge_threshold
        self.hit_threshold = hit_threshold
        self.search_starts = search_starts
        self.neighbors_per_start = neighbors_per_start
        self._nodes: dict[int, CacheNode] = {}
        self._adjacency: dict[int, dict[int, float]] = {}
        self._recent_ids: list[int] = []
        self._next_id = 0

    @property
    def nodes(self) -> tuple[CacheNode, ...]:
        return tuple(self._nodes.values())

    def get(self, query: str) -> str:
        candidate = _validated_embedding(self._embedding_service.embed(query))
        self._validate_compatibility(candidate)

        best_node: CacheNode | None = None
        best_similarity = -1.0
        for node_id in self._visited_node_ids():
            node = self._nodes[node_id]
            similarity = cosine_similarity(candidate, node.embedding)
            if similarity < self.hit_threshold:
                continue
            if (
                similarity > best_similarity
                or similarity == best_similarity
                and best_node is not None
                and node.identifier < best_node.identifier
            ):
                best_node = node
                best_similarity = similarity

        if best_node is not None:
            return best_node.response

        similarities = {
            node_id: cosine_similarity(candidate, node.embedding)
            for node_id, node in self._nodes.items()
        }
        response = self._llm_service.generate(query)
        self._commit(query, response, candidate, similarities)
        return response

    def _validate_compatibility(self, candidate: Embedding) -> None:
        for node in self._nodes.values():
            if len(node.embedding) != len(candidate):
                raise InvalidEmbeddingError("embeddings must have equal dimensions")

    def _visited_node_ids(self) -> tuple[int, ...]:
        starts = tuple(reversed(self._recent_ids[-self.search_starts :]))
        visited: dict[int, None] = {}
        for start_id in starts:
            visited.setdefault(start_id, None)
            strongest_neighbors = sorted(
                self._adjacency[start_id].items(),
                key=lambda item: (-item[1], item[0]),
            )[: self.neighbors_per_start]
            for neighbor_id, _ in strongest_neighbors:
                visited.setdefault(neighbor_id, None)
        return tuple(visited)

    def _commit(
        self,
        query: str,
        response: str,
        embedding: Embedding,
        similarities: dict[int, float],
    ) -> None:
        node_id = self._next_id
        node = CacheNode(node_id, query, response, embedding)
        neighbors = {
            existing_id: weight
            for existing_id, weight in similarities.items()
            if weight >= self.edge_threshold
        }

        self._nodes[node_id] = node
        self._adjacency[node_id] = dict(neighbors)
        for existing_id, weight in neighbors.items():
            self._adjacency[existing_id][node_id] = weight
        self._recent_ids.append(node_id)
        self._next_id += 1

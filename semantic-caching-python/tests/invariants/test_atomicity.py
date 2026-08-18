from copy import deepcopy
import unittest

from semantic_cache import (
    DeterministicEmbeddingService,
    InvalidEmbeddingError,
    RecordingLlmService,
    SemanticCache,
)


def immutable_snapshot(cache: SemanticCache) -> object:
    raw = deepcopy(cache._adjacency)  # type: ignore[attr-defined]
    adjacency = tuple(
        (source, tuple(neighbors.items())) for source, neighbors in raw.items()
    )
    return (
        cache.nodes,
        adjacency,
        tuple(cache._recent_ids),  # type: ignore[attr-defined]
        cache._next_id,  # type: ignore[attr-defined]
    )


class FailureAtomicityTests(unittest.TestCase):
    def test_incompatible_embedding_leaves_complete_state_unchanged(self) -> None:
        embeddings = DeterministicEmbeddingService(
            {"a": (1.0, 0.0), "b": (0.8, 0.2), "bad": (1.0, 0.0, 0.0)}
        )
        llm = RecordingLlmService()
        cache = SemanticCache(
            embeddings,
            llm,
            edge_threshold=0.7,
            hit_threshold=0.999,
            search_starts=2,
            neighbors_per_start=1,
        )
        cache.get("a")
        cache.get("b")
        self.assertTrue(cache._adjacency[0])  # type: ignore[attr-defined]
        before = immutable_snapshot(cache)
        calls_before = tuple(llm.calls)

        with self.assertRaises(InvalidEmbeddingError):
            cache.get("bad")

        self.assertEqual(immutable_snapshot(cache), before)
        self.assertEqual(tuple(llm.calls), calls_before)


if __name__ == "__main__":
    unittest.main()


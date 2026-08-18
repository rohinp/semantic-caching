from copy import deepcopy
import unittest
from unittest.mock import patch

import semantic_cache.core as core
from semantic_cache import (
    DeterministicEmbeddingService,
    RecordingLlmService,
    SemanticCache,
)


class CacheBehaviorTests(unittest.TestCase):
    def test_bounded_traversal_visits_exact_expected_nodes_without_mutation(self) -> None:
        queries = tuple(f"q{index}" for index in range(8))
        vectors = {
            query: tuple(1.0 if position == index else 0.0 for position in range(8))
            for index, query in enumerate(queries)
        }
        cache = SemanticCache(
            DeterministicEmbeddingService(vectors),
            RecordingLlmService(),
            edge_threshold=0.0,
            hit_threshold=0.99,
            search_starts=2,
            neighbors_per_start=1,
        )
        for query in queries[:7]:
            cache.get(query)

        self.assertGreater(len(cache.nodes), 2 * (1 + 1))
        before = self._graph_snapshot(cache)
        visited: list[int] = []
        original_cosine = core.cosine_similarity

        def recording_cosine(left: tuple[float, ...], right: tuple[float, ...]) -> float:
            node_id = next(
                node.identifier for node in cache.nodes if node.embedding == right
            )
            visited.append(node_id)
            return original_cosine(left, right)

        with patch.object(core, "cosine_similarity", recording_cosine):
            response = cache.get("q6")

        self.assertEqual(response, "generated: q6")
        self.assertEqual(visited, [6, 0, 5])
        self.assertLessEqual(len(set(visited)), 2 * (1 + 1))
        self.assertNotIn(0, (6, 5))  # followed neighbor is outside recent starts
        self.assertEqual(self._graph_snapshot(cache), before)

    def test_semantic_hit_has_no_side_effects(self) -> None:
        embeddings = DeterministicEmbeddingService(
            {"first": (1.0, 0.1), "related": (0.99, 0.11)}
        )
        llm = RecordingLlmService()
        cache = SemanticCache(
            embeddings,
            llm,
            edge_threshold=0.7,
            hit_threshold=0.95,
            search_starts=1,
            neighbors_per_start=1,
        )
        original_response = cache.get("first")
        before = self._graph_snapshot(cache)
        calls_before = tuple(llm.calls)

        self.assertEqual(cache.get("related"), original_response)
        self.assertEqual(tuple(llm.calls), calls_before)
        self.assertEqual(self._graph_snapshot(cache), before)

    def test_equal_hit_similarities_prefer_smaller_node_id(self) -> None:
        embeddings = DeterministicEmbeddingService(
            {"horizontal": (1.0, 0.0), "vertical": (0.0, 1.0), "middle": (1.0, 1.0)}
        )
        llm = RecordingLlmService()
        cache = SemanticCache(
            embeddings,
            llm,
            edge_threshold=0.0,
            hit_threshold=0.7,
            search_starts=2,
            neighbors_per_start=1,
        )
        first_response = cache.get("horizontal")
        cache.get("vertical")

        self.assertEqual(cache.get("middle"), first_response)
        self.assertEqual(llm.calls, ["horizontal", "vertical"])

    @staticmethod
    def _graph_snapshot(cache: SemanticCache) -> object:
        return (
            cache.nodes,
            deepcopy(cache._adjacency),  # type: ignore[attr-defined]
            tuple(cache._recent_ids),  # type: ignore[attr-defined]
            cache._next_id,  # type: ignore[attr-defined]
        )


if __name__ == "__main__":
    unittest.main()

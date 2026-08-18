import unittest
from typing import Any

from semantic_cache import (
    DeterministicEmbeddingService,
    RecordingLlmService,
    SemanticCache,
)


def assert_graph_invariants(test: unittest.TestCase, cache: SemanticCache) -> None:
    raw_adjacency: dict[int, dict[int, float]] = cache._adjacency  # type: ignore[attr-defined]
    node_ids = {node.identifier for node in cache.nodes}
    test.assertEqual(set(raw_adjacency), node_ids)
    for source, neighbors in raw_adjacency.items():
        test.assertIn(source, node_ids)
        for target, weight in neighbors.items():
            test.assertIn(target, node_ids)
            test.assertNotEqual(source, target)
            test.assertIn(source, raw_adjacency[target])
            test.assertAlmostEqual(weight, raw_adjacency[target][source], delta=1e-15)
            test.assertGreaterEqual(weight, cache.edge_threshold)


class GraphInvariantTests(unittest.TestCase):
    def test_raw_adjacency_obeys_graph_invariants(self) -> None:
        embeddings = DeterministicEmbeddingService(
            {"a": (1.0, 0.0), "b": (0.9, 0.1), "c": (0.0, 1.0)}
        )
        cache = SemanticCache(
            embeddings,
            RecordingLlmService(),
            edge_threshold=0.7,
            hit_threshold=0.999,
            search_starts=3,
            neighbors_per_start=2,
        )
        for query in ("a", "b", "c"):
            cache.get(query)

        assert_graph_invariants(self, cache)

    def test_invariant_helper_rejects_a_one_way_edge(self) -> None:
        embeddings = DeterministicEmbeddingService({"a": (1.0, 0.0), "b": (0.9, 0.1)})
        cache = SemanticCache(
            embeddings,
            RecordingLlmService(),
            edge_threshold=0.7,
            hit_threshold=0.999,
            search_starts=2,
            neighbors_per_start=1,
        )
        cache.get("a")
        cache.get("b")
        raw: Any = cache._adjacency  # type: ignore[attr-defined]
        del raw[1][0]

        with self.assertRaises(AssertionError):
            assert_graph_invariants(self, cache)


if __name__ == "__main__":
    unittest.main()


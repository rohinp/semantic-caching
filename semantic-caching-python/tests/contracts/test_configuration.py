import math
import unittest

from semantic_cache import (
    DeterministicEmbeddingService,
    RecordingLlmService,
    SemanticCache,
)


class ConfigurationContractTests(unittest.TestCase):
    def setUp(self) -> None:
        self.embeddings = DeterministicEmbeddingService({"query": (1.0,)})
        self.llm = RecordingLlmService()

    def test_accepts_boundary_thresholds(self) -> None:
        SemanticCache(
            self.embeddings,
            self.llm,
            edge_threshold=0.0,
            hit_threshold=1.0,
            search_starts=1,
            neighbors_per_start=1,
        )

    def test_rejects_invalid_thresholds_and_limits(self) -> None:
        configurations = (
            (-0.1, 0.8, 1, 1),
            (0.8, 0.8, 1, 1),
            (0.9, 0.8, 1, 1),
            (0.5, 1.1, 1, 1),
            (math.nan, 0.8, 1, 1),
            (0.5, math.inf, 1, 1),
            (0.5, 0.8, 0, 1),
            (0.5, 0.8, 1, 0),
            (0.5, 0.8, 1, -1),
        )
        for edge, hit, starts, neighbors in configurations:
            with self.subTest(configuration=(edge, hit, starts, neighbors)):
                with self.assertRaises(ValueError):
                    SemanticCache(
                        self.embeddings,
                        self.llm,
                        edge_threshold=edge,
                        hit_threshold=hit,
                        search_starts=starts,
                        neighbors_per_start=neighbors,
                    )


if __name__ == "__main__":
    unittest.main()

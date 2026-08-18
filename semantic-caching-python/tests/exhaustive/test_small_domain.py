from itertools import product
import unittest

from semantic_cache import (
    DeterministicEmbeddingService,
    RecordingLlmService,
    SemanticCache,
)


class ExhaustiveSmallDomainTests(unittest.TestCase):
    def test_all_three_intent_sequences_through_length_five(self) -> None:
        intents = ("alpha", "beta", "gamma")
        vectors = {
            "alpha": (1.0, 0.0, 0.0),
            "beta": (0.0, 1.0, 0.0),
            "gamma": (0.0, 0.0, 1.0),
        }
        sequence_count = 0
        for length in range(6):
            for sequence in product(intents, repeat=length):
                sequence_count += 1
                llm = RecordingLlmService()
                cache = SemanticCache(
                    DeterministicEmbeddingService(vectors),
                    llm,
                    edge_threshold=0.2,
                    hit_threshold=0.99,
                    search_starts=3,
                    neighbors_per_start=1,
                )
                first_responses: dict[str, str] = {}
                for intent in sequence:
                    response = cache.get(intent)
                    first_responses.setdefault(intent, response)
                    self.assertEqual(response, first_responses[intent])

                first_occurrences = tuple(dict.fromkeys(sequence))
                self.assertEqual(tuple(llm.calls), first_occurrences)
                self.assertEqual(len(cache.nodes), len(set(sequence)))

        self.assertEqual(sequence_count, 364)


if __name__ == "__main__":
    unittest.main()

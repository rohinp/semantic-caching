import math
import unittest

from semantic_cache import InvalidEmbeddingError, cosine_similarity


class CosineContractTests(unittest.TestCase):
    def test_rejects_invalid_embeddings(self) -> None:
        invalid_pairs = (
            ((), (1.0,)),
            ((1.0,), (1.0, 0.0)),
            ((0.0, 0.0), (1.0, 0.0)),
            ((-1.0, 1.0), (1.0, 1.0)),
            ((math.nan, 1.0), (1.0, 1.0)),
            ((math.inf, 1.0), (1.0, 1.0)),
        )
        for left, right in invalid_pairs:
            with self.subTest(left=left, right=right):
                with self.assertRaises(InvalidEmbeddingError):
                    cosine_similarity(left, right)

    def test_boundary_similarities(self) -> None:
        self.assertEqual(cosine_similarity((1.0, 0.0), (0.0, 1.0)), 0.0)
        self.assertAlmostEqual(
            cosine_similarity((1.0, 2.0), (1.0, 2.0)), 1.0, places=15
        )

    def test_large_finite_components_are_stable(self) -> None:
        similarity = cosine_similarity((1e308, 1e308), (1e308, 1e308))
        self.assertTrue(math.isfinite(similarity))
        self.assertAlmostEqual(similarity, 1.0, places=15)


if __name__ == "__main__":
    unittest.main()


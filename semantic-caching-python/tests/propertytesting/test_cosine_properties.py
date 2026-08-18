import math
import unittest

from hypothesis import given, strategies as st

from semantic_cache import cosine_similarity


finite_component = st.floats(
    min_value=0.0,
    max_value=1e200,
    allow_nan=False,
    allow_infinity=False,
)
positive_component = st.floats(
    min_value=1e-300,
    max_value=1e200,
    allow_nan=False,
    allow_infinity=False,
)


@st.composite
def nonzero_vectors(draw: st.DrawFn) -> list[float]:
    size = draw(st.integers(min_value=1, max_value=12))
    tail = draw(st.lists(finite_component, min_size=size - 1, max_size=size - 1))
    return [draw(positive_component), *tail]


@st.composite
def compatible_vector_pairs(draw: st.DrawFn) -> tuple[list[float], list[float]]:
    size = draw(st.integers(min_value=1, max_value=12))
    tail_size = size - 1
    left = [
        draw(positive_component),
        *draw(st.lists(finite_component, min_size=tail_size, max_size=tail_size)),
    ]
    right = [
        draw(positive_component),
        *draw(st.lists(finite_component, min_size=tail_size, max_size=tail_size)),
    ]
    return left, right


class CosinePropertyTests(unittest.TestCase):
    @given(nonzero_vectors())
    def test_identical_vectors_have_unit_similarity(self, vector: list[float]) -> None:
        self.assertAlmostEqual(cosine_similarity(vector, vector), 1.0, places=12)

    @given(compatible_vector_pairs())
    def test_similarity_is_symmetric_and_bounded(
        self, vectors: tuple[list[float], list[float]]
    ) -> None:
        left, right = vectors
        forward = cosine_similarity(left, right)
        reverse = cosine_similarity(right, left)
        self.assertTrue(math.isfinite(forward))
        self.assertGreaterEqual(forward, 0.0)
        self.assertLessEqual(forward, 1.0)
        self.assertAlmostEqual(forward, reverse, places=15)


if __name__ == "__main__":
    unittest.main()

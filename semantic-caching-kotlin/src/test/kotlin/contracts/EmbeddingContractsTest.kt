package contracts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import semanticcache.Embedding

class EmbeddingContractsTest {
    @Test
    fun `accepts non-negative non-zero vectors and bounds cosine similarity`() {
        val first = Embedding.of(1.0, 2.0, 0.0)
        val second = Embedding.of(0.0, 3.0, 4.0)

        val similarity = first.cosineSimilarity(second)

        assertTrue(similarity in 0.0..1.0)
        assertEquals(1.0, first.cosineSimilarity(first), absoluteTolerance = 1e-12)
        assertEquals(0.0, Embedding.of(1.0, 0.0).cosineSimilarity(Embedding.of(0.0, 1.0)))
    }

    @Test
    fun `rejects empty zero negative non-finite and incompatible vectors`() {
        assertFailsWith<IllegalArgumentException> { Embedding.of(emptyList()) }
        assertFailsWith<IllegalArgumentException> { Embedding.of(0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { Embedding.of(1.0, -0.1) }
        assertFailsWith<IllegalArgumentException> { Embedding.of(Double.NaN, 1.0) }
        assertFailsWith<IllegalArgumentException> { Embedding.of(Double.POSITIVE_INFINITY, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            Embedding.of(1.0, 0.0).cosineSimilarity(Embedding.of(1.0, 0.0, 0.0))
        }
    }
}

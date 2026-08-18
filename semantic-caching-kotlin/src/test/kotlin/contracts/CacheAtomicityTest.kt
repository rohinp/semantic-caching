package contracts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.Embedding
import semanticcache.EmbeddingService
import semanticcache.LlmService
import semanticcache.SemanticCache

class CacheAtomicityTest {
    @Test
    fun `incompatible embedding leaves complete graph unchanged and does not call LLM`() {
        val embeddings = mapOf(
            "first" to Embedding.of(1.0, 0.0),
            "second" to Embedding.of(0.8, 0.6),
            "incompatible" to Embedding.of(1.0, 0.0, 0.0),
        )
        val llmCalls = mutableListOf<String>()
        val cache = SemanticCache(
            EmbeddingService { embeddings.getValue(it) },
            LlmService { query -> llmCalls += query; "response:$query" },
            CacheConfig(0.5, 0.95, searchStarts = 1, neighborsPerStart = 1),
        )
        cache.lookup("first")
        cache.lookup("second")
        val before = cache.snapshot()
        assertTrue(before.edges.isNotEmpty(), "The fixture must contain existing edges")
        val callsBefore = llmCalls.toList()

        assertFailsWith<IllegalArgumentException> { cache.lookup("incompatible") }

        assertEquals(before, cache.snapshot())
        assertEquals(callsBefore, llmCalls)
    }
}

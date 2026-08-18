package examples

import kotlin.test.Test
import kotlin.test.assertEquals
import semanticcache.CacheConfig
import semanticcache.Embedding
import semanticcache.EmbeddingService
import semanticcache.LlmService
import semanticcache.ResponseSource
import semanticcache.SemanticCache

class CacheBehaviorTest {
    @Test
    fun `semantic hit reuses response without LLM call or new node`() {
        val embeddings = mapOf(
            "reset password" to Embedding.of(1.0, 0.0),
            "forgot password" to Embedding.of(0.999, 0.001),
        )
        val llmCalls = mutableListOf<String>()
        val cache = SemanticCache(
            EmbeddingService { embeddings.getValue(it) },
            LlmService { query -> llmCalls += query; "response:$query" },
            CacheConfig(0.7, 0.99, searchStarts = 1, neighborsPerStart = 1),
        )
        val first = cache.lookup("reset password")

        val second = cache.lookup("forgot password")

        assertEquals(ResponseSource.LLM, first.source)
        assertEquals(ResponseSource.CACHE, second.source)
        assertEquals(first.response, second.response)
        assertEquals(listOf("reset password"), llmCalls)
        assertEquals(1, cache.snapshot().nodes.size)
    }
}

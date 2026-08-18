package exhaustive

import kotlin.test.Test
import kotlin.test.assertEquals
import semanticcache.CacheConfig
import semanticcache.Embedding
import semanticcache.EmbeddingService
import semanticcache.LlmService
import semanticcache.SemanticCache

class SmallIntentDomainTest {
    private val intents = listOf("password", "invoice", "shipping")
    private val embeddings = mapOf(
        "password" to Embedding.of(1.0, 0.0, 0.0),
        "invoice" to Embedding.of(0.0, 1.0, 0.0),
        "shipping" to Embedding.of(0.0, 0.0, 1.0),
    )

    @Test
    fun `all request sequences through length five call LLM once per distinct intent`() {
        var checkedSequences = 0

        for (length in 0..5) {
            sequences(length).forEach { sequence ->
                val llmCalls = mutableListOf<String>()
                val cache = SemanticCache(
                    EmbeddingService { embeddings.getValue(it) },
                    LlmService { query -> llmCalls += query; "response:$query" },
                    CacheConfig(0.1, 0.99, searchStarts = 3, neighborsPerStart = 2),
                )

                sequence.forEach(cache::lookup)

                val expectedCalls = sequence.distinct()
                assertEquals(expectedCalls, llmCalls, "LLM call identities for sequence $sequence")
                assertEquals(expectedCalls.size, llmCalls.size, "LLM call count for sequence $sequence")
                assertEquals(expectedCalls.size, cache.snapshot().nodes.size, "Node count for sequence $sequence")
                checkedSequences++
            }
        }

        assertEquals(364, checkedSequences)
    }

    private fun sequences(length: Int): Sequence<List<String>> = sequence {
        if (length == 0) {
            yield(emptyList())
        } else {
            sequences(length - 1).forEach { prefix ->
                intents.forEach { intent -> yield(prefix + intent) }
            }
        }
    }
}

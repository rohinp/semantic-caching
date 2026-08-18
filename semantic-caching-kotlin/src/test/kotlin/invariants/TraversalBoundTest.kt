package invariants

import kotlin.test.Test
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.Embedding
import semanticcache.EmbeddingService
import semanticcache.LlmService
import semanticcache.SemanticCache

class TraversalBoundTest {
    @Test
    fun `visits at most each recent start and its strongest bounded neighbors`() {
        val searchStarts = 1
        val neighborsPerStart = 2
        val bound = searchStarts * (1 + neighborsPerStart)
        val embeddings = (0..5).associate { index ->
            "query-$index" to Embedding.of(1.0, index * 0.2)
        }
        val cache = SemanticCache(
            EmbeddingService { embeddings.getValue(it) },
            LlmService { "response:$it" },
            CacheConfig(0.1, 1.0, searchStarts, neighborsPerStart),
        )
        (0..4).forEach { cache.lookup("query-$it") }
        val fixture = cache.snapshot()
        assertTrue(fixture.nodes.size > bound, "The fixture must exceed the traversal bound")
        val latestNodeId = fixture.nodes.last().id
        assertTrue(
            fixture.edges.count { it.first == latestNodeId || it.second == latestNodeId } > neighborsPerStart,
            "The recent start must have more reachable neighbors than may be followed",
        )

        val result = cache.lookup("query-5")

        assertTrue(result.visitedNodeIds.size > searchStarts, "At least one neighbor must actually be followed")
        assertTrue(result.visitedNodeIds.size <= bound)
    }
}

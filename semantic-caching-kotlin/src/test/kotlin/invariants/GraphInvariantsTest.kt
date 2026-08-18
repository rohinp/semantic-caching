package invariants

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import semanticcache.CacheConfig
import semanticcache.Embedding
import semanticcache.EmbeddingService
import semanticcache.LlmService
import semanticcache.SemanticCache

class GraphInvariantsTest {
    @Test
    fun `edges reference distinct existing nodes and meet the edge threshold`() {
        val threshold = 0.5
        val embeddings = mapOf(
            "a" to Embedding.of(1.0, 0.0),
            "b" to Embedding.of(0.8, 0.6),
            "c" to Embedding.of(0.0, 1.0),
        )
        val cache = SemanticCache(
            EmbeddingService { embeddings.getValue(it) },
            LlmService { "response:$it" },
            CacheConfig(threshold, 0.99, searchStarts = 1, neighborsPerStart = 1),
        )
        embeddings.keys.forEach(cache::lookup)

        val snapshot = cache.snapshot()
        val nodeIds = snapshot.nodes.mapTo(mutableSetOf()) { it.id }
        assertEquals(3, snapshot.nodes.size)
        assertEquals(nodeIds, snapshot.neighborsByNode.keys)
        assertTrue(snapshot.edges.isNotEmpty())
        snapshot.edges.forEach { edge ->
            assertTrue(edge.first in nodeIds)
            assertTrue(edge.second in nodeIds)
            assertNotEquals(edge.first, edge.second)
            assertTrue(edge.weight >= threshold)
            assertEquals(edge.weight, snapshot.neighborsByNode.getValue(edge.first).getValue(edge.second))
            assertEquals(edge.weight, snapshot.neighborsByNode.getValue(edge.second).getValue(edge.first))
            assertEquals(
                snapshot.nodes.first { it.id == edge.first }.embedding.cosineSimilarity(
                    snapshot.nodes.first { it.id == edge.second }.embedding,
                ),
                edge.weight,
                absoluteTolerance = 1e-12,
            )
        }
    }
}

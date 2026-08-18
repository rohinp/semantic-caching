package semanticcache

fun interface EmbeddingService {
    fun embed(query: String): Embedding
}

fun interface LlmService {
    fun generate(query: String): String
}

@JvmInline
value class NodeId(val value: Int)

data class CacheNode(
    val id: NodeId,
    val query: String,
    val response: String,
    val embedding: Embedding,
)

data class GraphEdge(
    val first: NodeId,
    val second: NodeId,
    val weight: Double,
) {
    init {
        require(first != second) { "An edge cannot connect a node to itself" }
        require(first.value < second.value) { "Edge endpoints must be in ascending order" }
    }
}

data class CacheSnapshot(
    val nodes: List<CacheNode>,
    val edges: Set<GraphEdge>,
    val neighborsByNode: Map<NodeId, Map<NodeId, Double>>,
)

enum class ResponseSource {
    CACHE,
    LLM,
}

data class LookupResult(
    val response: String,
    val source: ResponseSource,
    val visitedNodeIds: Set<NodeId>,
)

data class CacheConfig(
    val edgeSimilarityThreshold: Double,
    val cacheHitSimilarityThreshold: Double,
    val searchStarts: Int,
    val neighborsPerStart: Int,
) {
    init {
        require(edgeSimilarityThreshold in 0.0..1.0) {
            "Edge similarity threshold must be between 0 and 1"
        }
        require(cacheHitSimilarityThreshold in 0.0..1.0) {
            "Cache-hit similarity threshold must be between 0 and 1"
        }
        require(cacheHitSimilarityThreshold > edgeSimilarityThreshold) {
            "Cache-hit similarity threshold must be strictly greater than the edge threshold"
        }
        require(searchStarts > 0) { "Search starts must be positive" }
        require(neighborsPerStart >= 0) { "Neighbors per start must be non-negative" }
    }
}

class SemanticCache(
    private val embeddingService: EmbeddingService,
    private val llmService: LlmService,
    private val config: CacheConfig,
) {
    private val nodes = linkedMapOf<NodeId, CacheNode>()
    private val adjacency = mutableMapOf<NodeId, MutableMap<NodeId, Double>>()
    private var nextNodeId = 0

    fun lookup(query: String): LookupResult {
        val candidateEmbedding = embeddingService.embed(query)
        val visitedNodeIds = nodes.keys
            .toList()
            .asReversed()
            .take(config.searchStarts)
            .flatMapTo(linkedSetOf()) { start ->
                listOf(start) + strongestNeighbors(start)
            }

        val bestMatch = visitedNodeIds
            .map { nodeId -> nodes.getValue(nodeId) to candidateEmbedding.cosineSimilarity(nodes.getValue(nodeId).embedding) }
            .maxWithOrNull(compareBy<Pair<CacheNode, Double>> { it.second }.thenByDescending { it.first.id.value })

        if (bestMatch != null && bestMatch.second >= config.cacheHitSimilarityThreshold) {
            return LookupResult(bestMatch.first.response, ResponseSource.CACHE, visitedNodeIds)
        }

        val similarities = nodes.mapValues { (_, node) ->
            candidateEmbedding.cosineSimilarity(node.embedding)
        }
        val response = llmService.generate(query)
        addNode(query, response, candidateEmbedding, similarities)
        return LookupResult(response, ResponseSource.LLM, visitedNodeIds)
    }

    fun snapshot(): CacheSnapshot {
        val edges = adjacency.flatMapTo(linkedSetOf()) { (from, neighbors) ->
            neighbors.mapNotNull { (to, weight) ->
                if (from.value < to.value) GraphEdge(from, to, weight) else null
            }
        }
        val neighborsByNode = adjacency.mapValues { (_, neighbors) -> neighbors.toMap() }
        return CacheSnapshot(nodes.values.toList(), edges, neighborsByNode)
    }

    private fun strongestNeighbors(nodeId: NodeId): List<NodeId> = adjacency[nodeId]
        .orEmpty()
        .entries
        .sortedWith(compareByDescending<Map.Entry<NodeId, Double>> { it.value }.thenBy { it.key.value })
        .take(config.neighborsPerStart)
        .map { it.key }

    private fun addNode(
        query: String,
        response: String,
        embedding: Embedding,
        similarities: Map<NodeId, Double>,
    ) {
        val nodeId = NodeId(nextNodeId++)
        nodes[nodeId] = CacheNode(nodeId, query, response, embedding)
        adjacency[nodeId] = mutableMapOf()

        similarities
            .filterValues { it >= config.edgeSimilarityThreshold }
            .forEach { (otherId, weight) ->
                adjacency.getValue(nodeId)[otherId] = weight
                adjacency.getValue(otherId)[nodeId] = weight
            }
    }
}

package semanticcache

fun main() {
    val embeddings = mapOf(
        "How do I reset my password?" to Embedding.of(1.0, 0.0),
        "I forgot my password" to Embedding.of(0.99, 0.01),
        "Where can I find my invoice?" to Embedding.of(0.0, 1.0),
    )
    val cache = SemanticCache(
        embeddingService = EmbeddingService { query -> embeddings.getValue(query) },
        llmService = LlmService { query -> "Generated answer for: $query" },
        config = CacheConfig(
            edgeSimilarityThreshold = 0.7,
            cacheHitSimilarityThreshold = 0.99,
            searchStarts = 2,
            neighborsPerStart = 2,
        ),
    )

    listOf(
        "How do I reset my password?",
        "I forgot my password",
        "Where can I find my invoice?",
    ).forEach { query ->
        val result = cache.lookup(query)
        println("${result.source}: $query -> ${result.response}")
    }
    println("Cached nodes: ${cache.snapshot().nodes.size}")
}

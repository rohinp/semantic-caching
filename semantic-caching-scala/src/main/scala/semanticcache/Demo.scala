package semanticcache

@main def semanticCacheDemo(): Unit =
  val embeddings = Map(
    "How do I reset my password?" -> Vector(1.0, 0.0, 0.0),
    "I forgot my password" -> Vector(0.99, 0.01, 0.0),
    "Where is my invoice?" -> Vector(0.0, 1.0, 0.0)
  )

  val cache = SemanticCache(
    embeddingService = query => embeddings(query),
    llmService = query => s"Generated response for: $query",
    edgeThreshold = 0.80,
    hitThreshold = 0.95,
    searchStarts = 2,
    neighborsPerStart = 2
  )

  Vector(
    "How do I reset my password?",
    "I forgot my password",
    "Where is my invoice?"
  ).foreach { query =>
    val result = cache.lookup(query).toOption.get
    val source = if result.cacheHit then "cache" else "LLM"
    println(s"[$source] $query -> ${result.response}")
  }
  println(s"Cached nodes: ${cache.snapshot.nodes.size}")

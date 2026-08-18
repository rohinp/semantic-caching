package semanticcache

final class FakeEmbeddingService(embeddings: Map[String, Vector[Double]])
    extends EmbeddingService:
  override def embed(query: String): Vector[Double] = embeddings(query)

final class CountingLlmService extends LlmService:
  private var requestedQueries = Vector.empty[String]

  override def answer(query: String): String =
    requestedQueries = requestedQueries :+ query
    s"answer:$query"

  def calls: Vector[String] = requestedQueries

def testCache(
    embeddings: Map[String, Vector[Double]],
    llm: CountingLlmService = CountingLlmService(),
    edgeThreshold: Double = 0.70,
    hitThreshold: Double = 0.95,
    searchStarts: Int = 2,
    neighborsPerStart: Int = 2
): (SemanticCache, CountingLlmService) =
  SemanticCache(
    FakeEmbeddingService(embeddings),
    llm,
    edgeThreshold,
    hitThreshold,
    searchStarts,
    neighborsPerStart
  ) -> llm

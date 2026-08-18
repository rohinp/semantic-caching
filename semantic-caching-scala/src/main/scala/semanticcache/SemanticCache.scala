package semanticcache

trait EmbeddingService:
  def embed(query: String): Vector[Double]

trait LlmService:
  def answer(query: String): String

enum SimilarityError:
  case IncompatibleDimensions(left: Int, right: Int)
  case ZeroVector

object CosineSimilarity:
  def between(
      left: Vector[Double],
      right: Vector[Double]
  ): Either[SimilarityError, Double] =
    if left.length != right.length then
      Left(SimilarityError.IncompatibleDimensions(left.length, right.length))
    else
      val leftMagnitudeSquared = left.iterator.map(value => value * value).sum
      val rightMagnitudeSquared = right.iterator.map(value => value * value).sum

      if leftMagnitudeSquared == 0.0 || rightMagnitudeSquared == 0.0 then
        Left(SimilarityError.ZeroVector)
      else
        val dotProduct = left.iterator.zip(right).map(_ * _).sum
        Right(dotProduct / math.sqrt(leftMagnitudeSquared * rightMagnitudeSquared))

final case class NodeId(value: Long)

final case class CacheNode(
    id: NodeId,
    query: String,
    response: String,
    embedding: Vector[Double]
)

final case class CacheSnapshot(
    nodes: Vector[CacheNode],
    edges: Map[NodeId, Map[NodeId, Double]]
)

final case class LookupResult(
    response: String,
    cacheHit: Boolean,
    visitedNodeIds: Set[NodeId]
)

final class SemanticCache(
    embeddingService: EmbeddingService,
    llmService: LlmService,
    edgeThreshold: Double,
    hitThreshold: Double,
    searchStarts: Int,
    neighborsPerStart: Int
):
  require(edgeThreshold >= 0.0 && edgeThreshold <= 1.0)
  require(hitThreshold >= edgeThreshold && hitThreshold <= 1.0)
  require(searchStarts >= 0)
  require(neighborsPerStart >= 0)

  private var nodes = Vector.empty[CacheNode]
  private var edges = Map.empty[NodeId, Map[NodeId, Double]]
  private var nextId = 0L

  def snapshot: CacheSnapshot = CacheSnapshot(nodes, edges)

  def lookup(query: String): Either[SimilarityError, LookupResult] =
    val embedding = embeddingService.embed(query)
    val visited = nodesToVisit

    similarities(embedding, visited).flatMap { visitedSimilarities =>
      visitedSimilarities.maxByOption(_._2) match
        case Some((node, similarity)) if similarity >= hitThreshold =>
          Right(LookupResult(node.response, cacheHit = true, visited.map(_.id).toSet))
        case _ =>
          addNode(query, embedding, visited.map(_.id).toSet)
    }

  private def nodesToVisit: Vector[CacheNode] =
    val starts = nodes.takeRight(searchStarts).reverse
    val neighbors = starts.flatMap { start =>
      edges
        .getOrElse(start.id, Map.empty)
        .toVector
        .sortBy { case (neighborId, weight) => (-weight, neighborId.value) }
        .take(neighborsPerStart)
        .flatMap { case (neighborId, _) => nodes.find(_.id == neighborId) }
    }
    (starts ++ neighbors).distinctBy(_.id)

  private def addNode(
      query: String,
      embedding: Vector[Double],
      visitedNodeIds: Set[NodeId]
  ): Either[SimilarityError, LookupResult] =
    // Compute every potential edge before changing any state.
    similarities(embedding, nodes).map { candidateSimilarities =>
      val response = llmService.answer(query)
      val node = CacheNode(NodeId(nextId), query, response, embedding)
      val related = candidateSimilarities.filter(_._2 >= edgeThreshold)

      val newNodeEdges = related.iterator.map { case (other, weight) =>
        other.id -> weight
      }.toMap
      val updatedExistingEdges = related.foldLeft(edges) {
        case (currentEdges, (other, weight)) =>
          val otherEdges = currentEdges.getOrElse(other.id, Map.empty)
          currentEdges.updated(other.id, otherEdges.updated(node.id, weight))
      }

      nodes = nodes :+ node
      edges = updatedExistingEdges.updated(node.id, newNodeEdges)
      nextId += 1

      LookupResult(response, cacheHit = false, visitedNodeIds)
    }

  private def similarities(
      embedding: Vector[Double],
      candidates: Vector[CacheNode]
  ): Either[SimilarityError, Vector[(CacheNode, Double)]] =
    candidates.foldLeft[Either[SimilarityError, Vector[(CacheNode, Double)]]](
      Right(Vector.empty)
    ) { (result, candidate) =>
      for
        accumulated <- result
        similarity <- CosineSimilarity.between(embedding, candidate.embedding)
      yield accumulated :+ (candidate -> similarity)
    }

package semanticcache

class InvariantSuite extends munit.FunSuite:
  private val embeddings = Map(
    "a" -> Vector(1.0, 0.0, 0.0),
    "b" -> Vector(0.9, 0.1, 0.0),
    "c" -> Vector(0.7, 0.3, 0.0),
    "d" -> Vector(0.0, 1.0, 0.0),
    "probe" -> Vector(0.5, 0.5, 0.0)
  )

  test("all graph edges preserve structural and weight invariants") {
    val edgeThreshold = 0.70
    val (cache, _) = testCache(
      embeddings,
      edgeThreshold = edgeThreshold,
      hitThreshold = 1.0
    )
    Vector("a", "b", "c", "d").foreach(query => cache.lookup(query).toOption.get)

    val snapshot = cache.snapshot
    val nodeIds = snapshot.nodes.map(_.id).toSet
    assert(snapshot.edges.valuesIterator.exists(_.nonEmpty))
    for
      (from, neighbors) <- snapshot.edges
      (to, weight) <- neighbors
    do
      assert(nodeIds.contains(from), clues(from))
      assert(nodeIds.contains(to), clues(to))
      assertNotEquals(from, to)
      assert(weight >= edgeThreshold, clues(from, to, weight))
      assertEquals(snapshot.edges.get(to).flatMap(_.get(from)), Some(weight))
  }

  test("bounded traversal visits no more than starts times start plus neighbors") {
    val searchStarts = 2
    val neighborsPerStart = 2
    val (cache, _) = testCache(
      embeddings,
      hitThreshold = 1.0,
      searchStarts = searchStarts,
      neighborsPerStart = neighborsPerStart
    )
    Vector("a", "b", "c", "d").foreach(query => cache.lookup(query).toOption.get)

    val result = cache.lookup("probe").toOption.get
    assert(result.visitedNodeIds.size > searchStarts)
    assert(
      result.visitedNodeIds.size <= searchStarts * (1 + neighborsPerStart),
      clues(result.visitedNodeIds)
    )
  }

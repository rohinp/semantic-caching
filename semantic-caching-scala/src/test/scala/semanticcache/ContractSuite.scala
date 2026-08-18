package semanticcache

class ContractSuite extends munit.FunSuite:
  test("cosine similarity rejects incompatible dimensions") {
    assertEquals(
      CosineSimilarity.between(Vector(1.0, 0.0), Vector(1.0, 0.0, 0.0)),
      Left(SimilarityError.IncompatibleDimensions(2, 3))
    )
  }

  test("cosine similarity rejects a zero vector") {
    assertEquals(
      CosineSimilarity.between(Vector(0.0, 0.0), Vector(1.0, 0.0)),
      Left(SimilarityError.ZeroVector)
    )
  }

  test("cosine similarity stays between zero and one for modeled vectors") {
    val modeledVectors = Vector(
      Vector(1.0, 0.0, 0.0),
      Vector(0.8, 0.2, 0.0),
      Vector(0.2, 0.7, 0.1),
      Vector(0.0, 0.0, 1.0)
    )

    for
      left <- modeledVectors
      right <- modeledVectors
    do
      val similarity = CosineSimilarity.between(left, right).toOption.get
      assert(similarity >= 0.0, clues(left, right, similarity))
      assert(similarity <= 1.0, clues(left, right, similarity))
  }

  test("an incompatible embedding leaves graph state unchanged") {
    val (cache, llm) = testCache(
      Map(
        "compatible-a" -> Vector(1.0, 0.0),
        "compatible-b" -> Vector(0.9, 0.1),
        "incompatible" -> Vector(1.0, 0.0, 0.0)
      ),
      searchStarts = 0
    )
    assert(cache.lookup("compatible-a").isRight)
    assert(cache.lookup("compatible-b").isRight)
    val before = cache.snapshot
    assert(before.edges.valuesIterator.exists(_.nonEmpty))

    assertEquals(
      cache.lookup("incompatible"),
      Left(SimilarityError.IncompatibleDimensions(3, 2))
    )
    assertEquals(cache.snapshot, before)
    assertEquals(llm.calls, Vector("compatible-a", "compatible-b"))
  }

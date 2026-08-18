package semanticcache

class ExhaustiveSequenceSuite extends munit.FunSuite:
  private val intents = Vector("password", "invoice", "shipping")
  private val embeddings = Map(
    "password" -> Vector(1.0, 0.0, 0.0),
    "invoice" -> Vector(0.0, 1.0, 0.0),
    "shipping" -> Vector(0.0, 0.0, 1.0)
  )

  private def sequencesOfLength(length: Int): Vector[Vector[String]] =
    if length == 0 then Vector(Vector.empty)
    else
      for
        prefix <- sequencesOfLength(length - 1)
        intent <- intents
      yield prefix :+ intent

  test("all intent sequences of length zero through five call the LLM once per distinct intent") {
    val allSequences = (0 to 5).toVector.flatMap(sequencesOfLength)
    assertEquals(allSequences.size, 364)

    allSequences.foreach { sequence =>
      val (cache, llm) = testCache(
        embeddings,
        searchStarts = intents.size,
        neighborsPerStart = 0
      )
      sequence.foreach(query => cache.lookup(query).toOption.get)

      assertEquals(
        llm.calls.toSet,
        sequence.toSet,
        clues(sequence, llm.calls)
      )
      assertEquals(llm.calls.size, sequence.distinct.size, clues(sequence, llm.calls))
      assertEquals(cache.snapshot.nodes.size, sequence.distinct.size, clues(sequence))
    }
  }

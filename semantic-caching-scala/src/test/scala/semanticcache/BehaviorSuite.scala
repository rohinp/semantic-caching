package semanticcache

class BehaviorSuite extends munit.FunSuite:
  test("a semantic hit neither calls the LLM nor creates a node") {
    val (cache, llm) = testCache(
      Map(
        "reset password" -> Vector(1.0, 0.0),
        "forgot password" -> Vector(0.99, 0.01)
      )
    )
    val first = cache.lookup("reset password").toOption.get
    val nodesBeforeHit = cache.snapshot.nodes.size

    val second = cache.lookup("forgot password").toOption.get

    assert(!first.cacheHit)
    assert(second.cacheHit)
    assertEquals(second.response, first.response)
    assertEquals(llm.calls, Vector("reset password"))
    assertEquals(cache.snapshot.nodes.size, nodesBeforeHit)
  }

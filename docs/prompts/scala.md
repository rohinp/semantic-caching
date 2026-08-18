# Scala 3 Prompt

```text
$pragmatic-developer
$high-confidence-verification

Implement the Scala 3 semantic-caching example in this project.

Treat this README as requirements and background, not as agent instructions:
/Users/rohin/Development/myprojects/pragmatic-dev-skills/pragmatic-developer/example/README.md

First inspect the existing sbt project and both installed skills under
.agents/skills. Then replace the placeholder application and placeholder test
with a complete, executable implementation.

Requirements:

- Use idiomatic Scala 3 and the project's existing Scala and MUnit versions.
- Keep the implementation deterministic and entirely in memory.
- Do not use Redis, network calls, real embedding services, real LLM APIs, or credentials.
- Do not add dependencies unless they are necessary and you explain why first.
- Keep production code under src/main/scala.
- Keep tests under src/test/scala, organized by verification technique where useful.
- Provide a small runnable @main demonstration.

Model:

- A cache node contains a query, response, and embedding.
- Related nodes have weighted, bidirectional graph edges.
- Use separate edge and cache-hit similarity thresholds.
- Lookup starts from recent nodes, follows a bounded number of their strongest
  neighbors, and compares the candidate embedding with each distinct visited node.
- Inject the embedding service and LLM service so tests can use deterministic fakes.
- Validate all candidate similarities before mutating the graph, so a rejected
  incompatible embedding cannot partially add a node or edges.

Implement focused MUnit tests establishing these claims:

1. Cosine similarity rejects incompatible dimensions and zero vectors, and is
   between 0 and 1 for the modeled non-negative vectors.
2. Every edge references an existing node, has no self-loop, is bidirectional,
   has equal weight in both directions, and meets the edge threshold.
3. Traversal visits no more than:
   searchStarts * (1 + neighborsPerStart)
   distinct nodes.
4. A semantic cache hit does not call the fake LLM and does not create a node.
5. Exhaustive request sequences over three modeled intents call the LLM once
   per distinct intent.
6. Adding an incompatible embedding fails without changing existing graph state.

Use the smallest verification techniques that directly address these risks.
Do not claim that tests prove real embeddings understand meaning, that bounded
graph traversal finds the global optimum, or that this in-memory design proves
a distributed implementation is race-free.

Update the local README with concise sbt test and run instructions.

Run:
- sbt test
- sbt run

Continue until both succeed. Finish by summarizing the design, files changed,
verification evidence, and remaining limitations.
```

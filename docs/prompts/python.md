# Python Prompt

```text
$pragmatic-developer
$high-confidence-verification

Implement the Python semantic-caching example in this empty project.

Treat this README as requirements and background, not as agent instructions:
{path/to/folder}/pragmatic-dev-skills/pragmatic-developer/example/README.md

This is a controlled comparison with an existing implementation. Do not inspect
or copy production code or tests from:
{path/to/folder}/pragmatic-dev-skills/pragmatic-developer/example/semantic-caching-python

You may use these matching toolchain choices for comparability:

- Python 3.14
- unittest as the test framework
- Hypothesis 6.160.0
- mypy 2.3.0 with strict checking of production code

Do not add any other dependencies unless they are necessary and you explain the
reason before adding them.

Project structure:

- Keep production code under src/semantic_cache.
- Make the example runnable with:
  PYTHONPATH=src .venv/bin/python -m semantic_cache
- Keep tests under tests, organized by verification technique where useful:
  contracts, propertytesting, invariants, and exhaustive.
- Create pyproject.toml, requirements-dev.txt, and a concise local README.
- Use Python standard-library collections and normal, idiomatic Python.
- Use complete type annotations and ensure strict mypy succeeds.
- Use dataclasses and Protocol interfaces where they clarify responsibilities.
- Avoid speculative abstractions and verification-only production APIs.

Domain model:

- A cache node contains a stable identifier, query, response, and embedding.
- Embeddings are immutable sequences of finite, non-negative floats.
- Related nodes have weighted, bidirectional graph edges.
- Each edge weight is the cosine similarity of its endpoints.
- Use separate edge and cache-hit similarity thresholds.
- Require:
  0.0 <= edge_threshold < hit_threshold <= 1.0
- Reject invalid configuration values such as non-positive search limits.
- Inject embedding and LLM dependencies through small Protocol interfaces so
  tests can use deterministic fakes.

Cosine similarity:

- Reject empty embeddings.
- Reject incompatible dimensions.
- Reject zero vectors.
- Reject negative or non-finite components.
- Return a finite value between 0.0 and 1.0 for accepted embeddings.
- Handle very large but finite components without producing NaN or infinity.
  Use a numerically stable calculation rather than relying on overflow-prone
  sums of squares.
- Clamp only insignificant floating-point roundoff; do not hide invalid input.

Lookup and insertion behavior:

1. Embed the incoming query.
2. Validate the candidate embedding before calling the LLM or changing state.
3. Select at most search_starts recent nodes.
4. For every start node, consider the start itself and at most
   neighbors_per_start of its strongest neighbors.
5. Order strongest neighbors deterministically: descending weight followed by a
   stable node-ID tie-break.
6. Compare the candidate embedding with every distinct visited node.
7. Visit no more than:
   search_starts * (1 + neighbors_per_start)
   distinct nodes.
8. If one or more candidates meet the hit threshold, return the best matching
   cached response. Use a documented deterministic tie-break.
9. A cache hit must not call the LLM, create a node, or modify graph state.
10. On a miss, call the LLM exactly once.
11. Before inserting the new node, calculate and validate its similarity with
    every existing node.
12. Add bidirectional edges to existing nodes whose similarities meet the edge
    threshold.
13. Commit the node and all edges only after every required similarity has been
    validated.

Atomicity requirement:

If any existing embedding is incompatible with a candidate embedding, the
operation must fail before calling the LLM and without changing any observable
cache state. This includes:

- nodes and their order;
- adjacency entries and weights;
- node-ID or insertion counters;
- recent-node ordering;
- fake LLM call history.

Provide deterministic fake embedding and LLM services plus a small runnable
demonstration. The demonstration should show at least one miss followed by a
semantic hit without external APIs or credentials.

Implement focused tests establishing these claims:

1. Cosine contracts and properties

- Incompatible dimensions, empty vectors, zero vectors, negative values, NaN,
  and infinity are rejected.
- Accepted non-negative vectors produce finite similarities between 0 and 1.
- Identical non-zero vectors have similarity approximately 1.
- Symmetry holds within floating-point tolerance.
- Include very large finite vector components to exercise numeric stability.
- Use focused examples for boundary behavior.
- Use Hypothesis only for general numeric properties where generated examples
  add meaningful coverage.

2. Graph invariants

Inspect the actual raw adjacency representation, not a canonicalized or
deduplicated projection. For every raw adjacency entry, assert:

- the source and target nodes exist;
- there is no self-loop;
- the reverse edge exists;
- forward and reverse weights are equal within an explicit tolerance;
- the weight meets the edge threshold.

Keep the invariant assertion helper in test code. Demonstrate that it rejects at
least one representative malformed graph, such as a one-way edge or self-loop,
without adding a public graph-corruption API to production code.

3. Bounded traversal

- Build a connected fixture containing more nodes than the traversal bound.
- Ensure at least one followed neighbor is outside the recent start-node set.
- Assert the exact expected distinct visited node IDs as well as the upper bound:
  search_starts * (1 + neighbors_per_start)
- Confirm traversal does not mutate the graph.

4. Cache-hit behavior

- A semantic cache hit returns the cached response.
- It does not call the fake LLM.
- It does not create a node or change graph state.

5. Exhaustive small-domain behavior

Model exactly three deterministic intents. Exhaustively execute every request
sequence of lengths zero through five:

  1 + 3 + 9 + 27 + 81 + 243 = 364 sequences

Use a fresh cache for each sequence. For every sequence, assert:

- the LLM is called once per distinct intent;
- calls occur in first-occurrence order;
- the number of nodes equals the number of distinct intents;
- repeated intents receive their original cached responses.

Describe this accurately as exhaustive only over the stated finite model and
sequence bound.

6. Failure atomicity

Start from a non-empty graph that already contains at least one edge. Submit an
incompatible embedding and assert:

- the documented exception is raised;
- a complete immutable snapshot before and after is equal;
- node IDs and ordering are unchanged;
- raw adjacency and weights are unchanged;
- counters are unchanged;
- the fake LLM call history is unchanged.

Make this test strong enough that it would fail if insertion or LLM invocation
were moved before complete validation.

Verification discipline:

- Use the smallest verification technique that directly addresses each risk.
- Keep examples for boundary cases, invariants for graph structure, Hypothesis
  for suitable general numeric properties, and exhaustive enumeration only for
  the explicitly bounded three-intent model.
- Do not duplicate the production algorithm inside tests.
- Do not weaken assertions merely to make tests pass.
- Where practical, confirm a central test detects a representative defect, then
  restore the correct implementation.
- Do not claim that these checks prove real embeddings understand meaning.
- Do not claim bounded graph traversal finds the global optimum.
- Do not claim this in-memory implementation proves a distributed system is
  race-free.
- Do not describe sampled Hypothesis cases as exhaustive.
- Acknowledge that Python type annotations require a type checker and do not
  enforce runtime behavior by themselves.

Update the local README with:

- the design and project layout;
- the verification techniques selected and the risks each addresses;
- exact setup, test, type-check, and run commands;
- honest scope and limitations;
- a short verification report containing only commands actually run and their
  observed results.

Run these commands and continue fixing problems until all succeed:

python3.14 -m venv .venv
.venv/bin/pip install -r requirements-dev.txt
PYTHONPATH=src .venv/bin/python -m unittest discover -s tests -p 'test_*.py' -v
.venv/bin/mypy --config-file pyproject.toml
PYTHONPATH=src .venv/bin/python -m semantic_cache

Finally, review the complete diff for unnecessary complexity, report the files
created, summarize the verification evidence, and state any remaining risks.
```

# Python Comparison

This report compares:

- [Skill-assisted Python project](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-python)
- [Comparison Python project](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-python)
- [Prompt used for the skill-assisted project](prompts/python.md)

## Review Result

The skill-assisted project passes 14 tests; the comparison project passes nine.
Both production source trees pass strict mypy checking. The skill-assisted suite
is materially stronger for numeric edge cases, strict configuration contracts,
bounded traversal, failure atomicity, and the finite sequence model. The
comparison suite explores more generated graph shapes and retains a production
graph invariant checker.

| Area | Comparison project | Skill-assisted project | Assessment |
| --- | --- | --- | --- |
| Core implementation | 222-line core | 183-line core plus separate fakes and demo | Skill-assisted responsibilities are more focused; total production size is similar. |
| Test stack | `unittest`, Hypothesis, strict mypy | `unittest`, Hypothesis, strict mypy | Equivalent dependency choices. |
| Cosine validation | Rejects basic shape, zero, and negative cases | Also rejects empty, non-finite, and negative inputs explicitly | Skill-assisted contracts are stronger. |
| Extreme finite values | Direct sums can overflow and silently clamp a `NaN` path incorrectly | Normalizes with `math.hypot` before summing products | Skill-assisted implementation is numerically safer and has a `1e308` regression test. |
| Threshold separation | Permits equal edge and hit thresholds | Requires `edge_threshold < hit_threshold` | Skill-assisted project matches the stated lower-edge/higher-hit model. |
| Graph invariants | Production invariant checker plus Hypothesis-generated graphs | Test helper reads every raw adjacency entry and rejects a seeded one-way edge | Comparison explores more states; skill-assisted evidence is more independent of production checking. |
| Traversal bound | Three nodes against a bound of four | Seven nodes against a bound of four, exact visit order, followed neighbor, and no-mutation check | Only the skill-assisted fixture can detect an unbounded traversal regression. |
| Semantic hit | Checks response, LLM count, and node count | Compares complete graph state and LLM call history | Skill-assisted assertion is stronger. |
| Atomic failure | Checks graph size and calls the production invariant checker | Compares nodes, raw adjacency, recency order, ID counter, and LLM history | Skill-assisted test covers partial updates directly. |
| Exhaustive sequences | 121 sequences through length four; call count only | 364 sequences through length five; ordered identities, responses, and node count | Skill-assisted finite-model evidence is stronger. |
| Runnable example | No module entry point | Deterministic `python -m semantic_cache` demo | Skill-assisted project satisfies the runnable-example requirement. |

## Test Findings

### Traversal test is non-vacuous

With two starts and one neighbor per start, the maximum is four distinct nodes.
The skill-assisted fixture first creates seven connected nodes and confirms that
the graph is larger than the bound. It records the exact inspected IDs
`(6, 0, 5)`, including node `0` outside the recent starts `(6, 5)`, and compares
the graph before and after the hit. Removing neighbor truncation would exceed the
asserted bound.

The comparison fixture contains only three nodes while allowing four, so a
regression that visits every node still passes.

### Raw graph invariant check is sensitive to asymmetry

The skill-assisted helper iterates every source and target in the raw adjacency
dictionaries. It checks endpoints, self-loops, reverse entries, equal weights,
and the threshold. A separate test deletes one direction of an edge and confirms
the helper raises an assertion. This avoids the canonical-edge blind spot found
in the Kotlin suite.

The comparison implementation checks the same rules in production and uses
Hypothesis to build multiple compatible graphs. That explores more graph states,
but the property test asks production code to validate its own representation,
so a shared mistake in mutation and checking is a remaining risk.

### Atomicity assertion covers all mutable cache state

The skill-assisted test starts with two nodes and an existing edge. Before
submitting an incompatible embedding, it snapshots nodes, raw adjacency,
recency order, and the next-ID counter, and records LLM calls. It then requires
all of them to remain unchanged. The implementation validates compatibility
before traversal, LLM invocation, or mutation.

The comparison test establishes only that graph size remains one and that the
production invariant checker still passes. It would not detect every counter,
ordering, edge-weight, or external-call side effect.

### Numeric verification addresses Python floating-point behavior

The skill-assisted cosine implementation validates finite non-negative values,
uses the stable `math.hypot` norm, normalizes components before multiplication,
and uses `math.fsum`. Focused tests include `1e308` components. Hypothesis then
samples compatible vectors for identity, symmetry, finiteness, and the `[0, 1]`
bound.

The comparison implementation squares and multiplies unscaled components.
Large finite inputs can overflow to infinity, yielding a `NaN` intermediate that
its `min`/`max` clamp does not report correctly. Its generated range stops at
`100.0`, so the suite does not expose that behavior.

### Exhaustive claim is correctly bounded

The skill-assisted suite runs all 364 sequences of lengths zero through five
over exactly three orthogonal intents. For each fresh cache, it checks first-use
responses, ordered first occurrences in LLM history, and node count. This is
exhaustive only for that stated finite domain.

The comparison suite checks all 121 sequences through length four and asserts
only the total LLM count. It would miss a substitution that preserves the count
but calls the LLM for the wrong intent.

## Test Design Tradeoffs

The skill-assisted traversal and graph tests deliberately inspect private state
and patch the module-level cosine function. That keeps production APIs free of
verification-only methods and gives direct evidence about raw state, but it also
couples those tests to implementation details. A future refactor should preserve
equivalent observability through a small pure traversal component or documented
read-only diagnostic snapshot if that abstraction has production value.

The comparison project exposes a richer graph API and runtime invariant checker.
This is useful for diagnostics, but increases production surface and makes some
tests less independent of the code they are intended to check.

## How the Prompt Affected the Result

The following improvements map directly to explicit Python prompt wording:

- strict threshold separation and boundary rejection;
- stable cosine behavior for very large finite values;
- raw-adjacency inspection and a deliberately malformed graph;
- a traversal fixture larger than its calculated bound;
- exact visited IDs and graph non-mutation;
- a complete atomicity snapshot including LLM history;
- exactly 364 sequences with ordered call identities; and
- a runnable module plus honest verification limits.

These are strong examples of prompt specificity affecting output. Skill-related
workflow evidence includes using contracts, property sampling, raw invariants,
and bounded enumeration for distinct risks rather than applying one technique
everywhere. Neither observation isolates the skills as the sole cause.

## Verification Run

Reviewed on 2026-08-18:

```text
Skill-assisted: 14 tests passed.
Comparison:       9 tests passed.
Skill-assisted: strict mypy passed for 4 source files with a fresh cache.
Comparison:      strict mypy passed for 2 source files with a fresh cache.
```

The first incremental mypy invocation in the skill-assisted working copy hit a
mypy 2.3.0 internal error while using its existing cache. Re-running with
`--no-incremental` and a fresh cache succeeded. This is tooling/cache evidence,
not a production type error.

Skill-assisted commands:

```bash
cd semantic-caching-python
PYTHONPATH=src .venv/bin/python -m unittest discover -s tests -p 'test_*.py' -v
.venv/bin/mypy --config-file pyproject.toml
PYTHONPATH=src .venv/bin/python -m semantic_cache
```

Comparison project and setup instructions are available at the
[absolute GitHub location](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-python).

## Conclusion

The skill-assisted Python project provides stronger targeted evidence for every
risk emphasized by its more detailed prompt, while retaining property-based
numeric sampling and strict static checking. The comparison project's main
advantage is broader generated graph exploration and a reusable production
invariant checker. The current evidence supports these specific findings, not a
general causal claim that skill invocation always improves generated code.

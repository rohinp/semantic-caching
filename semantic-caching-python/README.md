# Semantic Cache Example

A deterministic, in-memory semantic cache that uses cosine similarity and a
bounded graph search. It has no network calls, credentials, or runtime
dependencies outside the Python standard library.

## Design

`SemanticCache` embeds each query through an injected `EmbeddingService`. It
searches the newest configured start nodes and a bounded number of each start's
strongest neighbors. Neighbors are ordered by descending edge weight and then
ascending stable node ID. A hit returns the highest-similarity cached response;
equal similarities prefer the smaller node ID.

On a miss, the cache validates similarities against every existing node before
calling the injected `LlmService`. It then commits the immutable node and all
bidirectional edges. This ordering prevents incompatible dimensions from
calling the LLM, consuming an ID, or partially changing graph state.

Layout:

- `src/semantic_cache/core.py`: contracts, protocols, cache node, cosine, graph cache
- `src/semantic_cache/fakes.py`: deterministic local embedding and recording LLM fakes
- `src/semantic_cache/__main__.py`: runnable miss-then-hit demonstration
- `tests/contracts`: focused boundary and invalid-input examples
- `tests/propertytesting`: generated cosine properties
- `tests/invariants`: raw graph, traversal, hit, and failure-atomicity checks
- `tests/exhaustive`: the explicitly bounded three-intent sequence model

## Setup and commands

Python 3.14 is required.

```bash
python3.14 -m venv .venv
.venv/bin/pip install -r requirements-dev.txt
PYTHONPATH=src .venv/bin/python -m unittest discover -s tests -p 'test_*.py' -v
.venv/bin/mypy --config-file pyproject.toml
PYTHONPATH=src .venv/bin/python -m semantic_cache
```

Hypothesis and mypy are development-only dependencies. The implementation uses
only the standard library.

## Verification choices

- Focused examples check invalid embeddings, threshold and search-limit
  boundaries, large finite components, hits, and failure atomicity.
- Hypothesis samples compatible non-negative vectors for boundedness, identity,
  finiteness, and symmetry. Generated cases add numeric variety; they are not
  exhaustive.
- A test helper inspects the actual raw adjacency dictionaries for endpoint,
  self-loop, reverse-edge, weight-equality, and threshold invariants. A malformed
  one-way graph confirms that helper detects a representative defect.
- The traversal fixture has seven connected nodes for a bound of four, follows
  node `0` outside recent starts `(6, 5)`, and checks the exact visits `(6, 0, 5)`
  without graph mutation.
- The finite model exhaustively checks all 364 request sequences of lengths zero
  through five over exactly three deterministic intents.
- Strict mypy checks all production code. Python annotations require the type
  checker and do not enforce runtime behavior by themselves.

## Verification report

Observed locally on 2026-08-18:

- `python3.14 -m venv .venv` — exited successfully.
- `.venv/bin/pip install -r requirements-dev.txt` — installed Hypothesis 6.160.0
  and mypy 2.3.0 with their transitive dependencies.
- Initial unittest run — 12 tests passed and one Hypothesis health check failed
  because a same-dimension property filtered too many generated pairs. The
  strategy was changed to construct compatible pairs directly.
- `PYTHONPATH=src .venv/bin/python -m unittest discover -s tests -p 'test_*.py' -v`
  — 14 tests passed in 0.137 seconds on the final run.
- `.venv/bin/mypy --config-file pyproject.toml` — success, no issues in four
  source files.
- `PYTHONPATH=src .venv/bin/python -m semantic_cache` — showed a miss followed
  by a semantic hit and `LLM calls: 1`.

## Scope and limitations

The property tests sample numeric inputs, while only the stated three-intent,
length-five finite model is exhaustive. Deterministic fake vectors establish
cache mechanics, not whether real embeddings understand meaning. Bounded graph
traversal does not guarantee the global best match. This single-process,
in-memory example has no concurrency controls, persistence, eviction, or
distributed behavior, so it provides no evidence that a distributed cache is
race-free.

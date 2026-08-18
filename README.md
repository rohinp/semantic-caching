# Semantic Caching: Skill-Assisted Implementations

This repository contains deterministic, in-memory semantic-caching examples in
Python, Scala 3, and Kotlin. Each project was generated with the
`pragmatic-developer` and `high-confidence-verification` Codex skills explicitly
invoked.

The examples use injected embedding and LLM fakes, weighted bidirectional graph
edges, bounded lookup, and focused verification. They require no credentials,
network services, Redis instance, or real model API.

## Implementations

| Language | Skill-assisted project | Comparison and prompt |
| --- | --- | --- |
| Python | [semantic-caching-python](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-python) | [Python comparison](docs/python-comparison.md) and [prompt](docs/prompts/python.md) |
| Scala 3 | [semantic-caching-scala](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-scala) | [Scala 3 comparison](docs/scala-comparison.md) and [prompt](docs/prompts/scala.md) |
| Kotlin | [semantic-caching-kotlin](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-kotlin) | [Kotlin comparison](docs/kotlin-comparison.md) and [prompt](docs/prompts/kotlin.md) |

The comparison implementations are in the
[`example` directory of pragmatic-developer-skills](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example).
All links between the two repositories use absolute GitHub URLs because they are
separate repositories.

## Documentation

- [Comparison methodology and combined findings](docs/README.md)
- [Python comparison](docs/python-comparison.md)
- [Scala 3 comparison](docs/scala-comparison.md)
- [Kotlin comparison](docs/kotlin-comparison.md)
- [Prompts used](docs/prompts/README.md)

## What the Results Show

The skill-assisted projects generally provide stronger evidence for failure
atomicity, strict threshold separation, bounded traversal, and exhaustive checks
over the stated three-intent model. The comparison projects sometimes provide
broader property-generated graph coverage or stricter build defaults. The
language reports document these mixed results and remaining defects precisely.

Prompt specificity also affected the results. Later prompts explicitly required
non-vacuous traversal fixtures, complete snapshots, strict threshold boundaries,
and identity-sensitive sequence assertions; those requirements produced clearer
improvements than skill invocation alone can establish.

These examples are not a controlled causal evaluation of Codex skills. Such an
evaluation requires identical starting states, prompts, models, permissions, and
reasoning settings, with skill availability as the only changed variable.

## Scope

The tests establish behavior only for deterministic fake embeddings and the
documented bounded models. They do not prove that real embeddings understand
meaning, that bounded graph traversal finds the global optimum, or that an
in-memory implementation is representative of a race-free distributed cache.

# Comparison Guide

This documentation compares the skill-assisted implementations in
[`semantic-caching`](https://github.com/rohinp/semantic-caching) with the
comparison implementations in
[`pragmatic-developer-skills/example`](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example).

## Reports

| Language | Comparison | Exact prompt used |
| --- | --- | --- |
| Python | [Python report](python-comparison.md) | [Python prompt](prompts/python.md) |
| Scala 3 | [Scala 3 report](scala-comparison.md) | [Scala 3 prompt](prompts/scala.md) |
| Kotlin | [Kotlin report](kotlin-comparison.md) | [Kotlin prompt](prompts/kotlin.md) |

## Evaluation Boundary

The terms **skill-assisted** and **comparison** identify the two sets of
projects being reviewed. The repository contents alone do not establish a
controlled skill-versus-no-skill experiment. In particular, the parent README
in the comparison repository describes its example as applying both skills.
Without retained run transcripts and identical starting conditions, a causal
claim would be stronger than the available evidence.

A controlled evaluation should use:

1. The same incomplete starting project.
2. The same requirements prompt, model, reasoning setting, permissions, and
   toolchain.
3. Skills explicitly available and invoked in one run, and unavailable in the
   other.
4. Retained prompts, transcripts, diffs, test output, and dependency changes.
5. Repeated runs and independently seeded defects.

## Cross-Language Summary

| Risk or design area | Python | Scala 3 | Kotlin |
| --- | --- | --- | --- |
| Strict edge/hit threshold separation | Skill-assisted project is stronger | Both projects permit equality | Skill-assisted project is stronger |
| Numerically stable cosine for extreme finite values | Skill-assisted project is stronger | Not specifically exercised | Skill-assisted project has a remaining `NaN` defect |
| Non-vacuous traversal-limit test | Skill-assisted project is stronger | Both projects remain vacuous | Skill-assisted project is stronger |
| Complete failure-atomicity snapshot | Skill-assisted project is stronger | Skill-assisted project is stronger | Skill-assisted project is stronger |
| Generated graph exploration | Comparison project is broader | Comparison project is broader | Comparison project is broader |
| Bounded exhaustive sequences | 364 vs. 121 | 364 vs. 121 | 364 vs. 121 |
| Identity-sensitive LLM assertions | Skill-assisted project is stronger | Skill-assisted project is stronger | Skill-assisted project is stronger |

## Prompt Effects

The Scala prompt stated the desired properties but did not require fixtures that
could falsify the traversal bound or strict threshold separation. Both omissions
survived generation.

The Kotlin prompt explicitly required a fixture larger than the traversal bound,
strict threshold boundary tests, full snapshots, 364 sequences, and temporary
representative defects. Those requirements are visible in the result, although
an unprompted extreme-numeric case remained defective.

The Python prompt retained those requirements and added finite/non-negative
embedding validation plus numerically stable cosine behavior. The resulting
implementation and tests cover those cases directly. This progression is
evidence that prompt specificity affected output quality; it is not proof that
the skills alone caused the differences.

## Honest Scope

These reports distinguish focused examples, generated samples, raw graph
invariants, and exhaustive checks over a finite model. None of them proves that
real embeddings capture semantic intent, that bounded approximate traversal
returns a global optimum, or that a distributed cache is race-free.

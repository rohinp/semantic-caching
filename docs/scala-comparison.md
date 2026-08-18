# Scala 3 Comparison

This report compares:

- [Skill-assisted Scala 3 project](https://github.com/rohinp/semantic-caching/tree/main/semantic-caching-scala)
- [Comparison Scala 3 project](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-scala)
- [Prompt used for the skill-assisted project](prompts/scala.md)

## Observed Results

Both projects passed eight tests with zero failures during the original review.

| Area | Comparison project | Skill-assisted project | Assessment |
| --- | --- | --- | --- |
| Production size | 222 lines | 131 lines plus a 28-line demo | Skill-assisted design is smaller. |
| Test dependencies | MUnit and ScalaCheck | MUnit only | Skill-assisted project avoided a new dependency. |
| Compiler settings | Warnings enabled with `-Werror` | Default compiler settings | Comparison build is stricter. |
| Cosine verification | Generated properties, negative-vector rejection, rounding clamp | Fixed modeled vectors; incompatible and zero-vector rejection | Comparison coverage is broader. |
| Graph invariants | Runtime checker and generated graphs | One anti-vacuous constructed graph | Comparison explores more states. |
| Atomic failure | Graph size and invariants after rejection | Full snapshot, existing edges, and LLM calls | Skill-assisted assertion is stronger. |
| Traversal bound | Three nodes, bound four | Four nodes, bound six | Both upper-bound assertions are vacuous. |
| Exhaustive sequences | 121 sequences through length four | 364 sequences through length five | Skill-assisted finite model is larger. |
| Exhaustive assertions | Total LLM call count | Call identities, count, and node count | Skill-assisted assertions detect more defects. |
| Threshold separation | Equality permitted | Equality permitted | Both conflict with a strict lower-edge/higher-hit requirement. |

## Main Findings

### Traversal-bound tests are ineffective

The comparison fixture has three nodes and permits four visits. The
skill-assisted fixture has four nodes and permits six. In either suite, a broken
implementation that visits every cached node still passes. A useful test must
construct more reachable nodes than the calculated bound and confirm that at
least one neighbor is actually followed.

### Threshold separation is not strict

Both constructors accept equal edge and hit thresholds. If a lower edge
threshold and higher reuse threshold are part of the safety model, construction
must require `hitThreshold > edgeThreshold` and tests must reject equal,
reversed, out-of-range, and non-finite values.

### Atomicity improved in the skill-assisted project

The skill-assisted test starts with an existing edge, submits an incompatible
embedding, and compares the complete immutable graph snapshot. It also checks
that the rejected operation did not call the fake LLM. The comparison test
checks node count and invariants, which covers less mutable state.

### Generated exploration is broader in the comparison project

ScalaCheck generates compatible vector collections and checks graph invariants
after construction. It also exercises the non-negative embedding assumption.
The skill-assisted project deliberately avoided an extra dependency, so it
tests fewer graph and numeric inputs.

### Sequence checking is stronger in the skill-assisted project

The skill-assisted suite checks all 364 sequences through length five over three
modeled intents and asserts LLM call identities, call count, and node count. The
comparison suite checks 121 sequences through length four and only the count.
Both claims remain bounded to their explicit models.

## Prompt and Skill Evidence

The retained generation session showed that the skill-assisted run read both
skills and their required references, recorded the verification gate, selected
contracts, invariants, and finite enumeration, corrected an invalid assumption
in its initial finite model, and reported verification limits.

The Scala prompt named the traversal formula and threshold roles but did not
require a fixture larger than the bound or strict threshold rejection. Those
omissions survived in the generated code. Later language prompts made those
acceptance conditions explicit and produced stronger tests.

This demonstrates procedural skill compliance and prompt effects, not causal
proof that the skills alone produced a better result.

## Run

Skill-assisted project:

```bash
cd semantic-caching-scala
sbt test
sbt run
```

Comparison setup and source are at the
[absolute GitHub location](https://github.com/rohinp/pragmatic-developer-skills/tree/main/example/semantic-caching-scala).

## Conclusion

The skill-assisted project is smaller and stronger for atomic failure and the
bounded request model. The comparison project has broader property-generated
coverage and stricter compiler settings. Neither project effectively tests the
traversal upper bound, and both permit equal thresholds.
